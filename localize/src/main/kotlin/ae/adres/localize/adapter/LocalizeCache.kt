package ae.adres.localize.adapter

import ae.adres.localize.LocalizeConfig
import ae.adres.localize.domain.LocalizeStore
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/** Interface for disk cache of localization data. */
interface LocalizeCache {
    suspend fun load(locale: String): LocalizeStore?

    suspend fun save(store: LocalizeStore)
}

/**
 * Disk cache: one file per locale: {cacheDir}/localize_{hash}_{platform}_{locale}.json
 */
class FileLocalizeCache(
    private val config: LocalizeConfig,
    private val cacheDir: File,
) : LocalizeCache {
    private val prefix = "localize_${hashPrefix(config.apiKey)}_${config.platform}"

    private fun cacheFileForLocale(locale: String): File = File(cacheDir, "${prefix}_$locale.json")

    override suspend fun load(locale: String): LocalizeStore? =
        withContext(Dispatchers.IO) {
            val file = cacheFileForLocale(locale)
            if (!file.exists()) return@withContext tryMigrateFromLegacy(locale)
            try {
                val content = file.readText()
                if (content.isEmpty()) return@withContext null
                val json =
                    com.google.gson.JsonParser
                        .parseString(content)
                        .asJsonObject
                parseLocaleFile(json, locale)
            } catch (_: Exception) {
                null
            }
        }

    private suspend fun tryMigrateFromLegacy(locale: String): LocalizeStore? =
        withContext(Dispatchers.IO) {
            val legacyFile = File(cacheDir, "$prefix.json")
            if (!legacyFile.exists()) return@withContext null
            try {
                val content = legacyFile.readText()
                if (content.isEmpty()) return@withContext null
                val json =
                    com.google.gson.JsonParser
                        .parseString(content)
                        .asJsonObject
                val full = parseLegacyStore(json) ?: return@withContext null
                save(full)
                legacyFile.delete()
                val simple = full.simple[locale]?.let { mapOf(locale to it) } ?: emptyMap()
                val plural = full.plural[locale]?.let { mapOf(locale to it) } ?: emptyMap()
                LocalizeStore(simple = simple, plural = plural)
            } catch (_: Exception) {
                null
            }
        }

    private fun parseLocaleFile(
        json: JsonObject,
        locale: String,
    ): LocalizeStore? {
        val simpleRaw = json.getAsJsonObject("simple")
        val simple =
            mapOf(
                locale to (simpleRaw?.keySet()?.associateWith { simpleRaw.get(it).asString } ?: emptyMap()),
            )
        val pluralRaw = json.getAsJsonObject("plural")
        val pluralInner = mutableMapOf<String, Map<String, String>>()
        pluralRaw?.keySet()?.forEach { key ->
            val forms = pluralRaw.getAsJsonObject(key)
            forms?.keySet()?.associateWith { forms.get(it).asString }?.let { pluralInner[key] = it }
        }
        val plural = mapOf(locale to pluralInner)
        return LocalizeStore(simple = simple, plural = plural)
    }

    private fun parseLegacyStore(json: JsonObject): LocalizeStore? {
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
                forms?.keySet()?.associateWith { forms.get(it).asString }?.let { pluralInner[key] = it }
            }
            plural[locale] = pluralInner
        }
        return LocalizeStore(simple = simple, plural = plural)
    }

    override suspend fun save(store: LocalizeStore) =
        withContext(Dispatchers.IO) {
            val allLocales = store.simple.keys + store.plural.keys
            for (locale in allLocales) {
                try {
                    val json =
                        mapOf(
                            "locale" to locale,
                            "simple" to (store.simple[locale] ?: emptyMap()),
                            "plural" to (store.plural[locale] ?: emptyMap()),
                        )
                    cacheFileForLocale(locale).writeText(Gson().toJson(json))
                } catch (_: Exception) {
                }
            }
        }

    companion object {
        internal fun hashPrefix(apiKey: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(apiKey.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }.take(8)
        }
    }
}
