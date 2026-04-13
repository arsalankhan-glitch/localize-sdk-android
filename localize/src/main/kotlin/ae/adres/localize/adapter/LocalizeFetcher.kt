package ae.adres.localize.adapter

import ae.adres.localize.LocalizeConfig
import ae.adres.localize.domain.LocalizeStore
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/** Interface for fetching localization data from the export endpoint. */
interface LocalizeFetcher {
    suspend fun fetch(): LocalizeStore?
}

/** Fetches localization data from GET /sdk/export. */
class OkHttpLocalizeFetcher(
    private val config: LocalizeConfig,
    private val client: OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build(),
) : LocalizeFetcher {
    private val exportUrl = "${config.normalizedBaseUrl}/sdk/export?platform=${java.net.URLEncoder.encode(config.platform, "UTF-8")}"

    override suspend fun fetch(): LocalizeStore? =
        withContext(Dispatchers.IO) {
            logRequest()
            val request =
                Request
                    .Builder()
                    .url(exportUrl)
                    .addHeader("X-API-Key", config.apiKey)
                    .get()
                    .build()

            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""
                    logResponse(response.code, body)
                    parseResponse(response.code, body)
                }
            } catch (e: Exception) {
                logError(e)
                null
            }
        }

    private fun parseResponse(
        statusCode: Int,
        body: String,
    ): LocalizeStore? {
        if (statusCode in listOf(401, 403, 404)) return null
        if (statusCode >= 500) return null
        if (statusCode != 200) return null

        if (body.isEmpty()) return LocalizeStore()

        return try {
            val json = JsonParser.parseString(body).asJsonObject
            parseStore(json)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseStore(json: JsonObject): LocalizeStore? {
        val languages = json.getAsJsonObject("languages") ?: return null
        val simple = mutableMapOf<String, Map<String, String>>()
        val plural = mutableMapOf<String, Map<String, Map<String, String>>>()

        for (locale in languages.keySet()) {
            val langData = languages.getAsJsonObject(locale) ?: continue
            val simpleRaw = langData.getAsJsonObject("simple")
            simple[locale] = simpleRaw?.keySet()?.associateWith { simpleRaw.get(it).asString } ?: emptyMap()

            val pluralRaw = langData.getAsJsonObject("plural")
            val pluralInner = mutableMapOf<String, Map<String, String>>()
            pluralRaw?.keySet()?.forEach { key ->
                val forms = pluralRaw.getAsJsonObject(key)
                forms?.keySet()?.associateWith { forms.get(it)?.asString ?: "" }?.let { pluralInner[key] = it }
            }
            plural[locale] = pluralInner
        }
        return LocalizeStore(simple = simple, plural = plural)
    }

    private fun logRequest() {
        if (!config.enableLogging) return
        val header = if (config.apiKey.isEmpty()) "" else "***"
        println("[LocalizeSDK] GET $exportUrl Headers: X-API-Key: $header")
    }

    private fun logResponse(
        statusCode: Int,
        body: String,
    ) {
        if (!config.enableLogging) return
        println("[LocalizeSDK] Status: $statusCode Response: ${body.take(200)}...")
    }

    private fun logError(e: Exception) {
        if (!config.enableLogging) return
        println("[LocalizeSDK] Error: $e")
    }
}
