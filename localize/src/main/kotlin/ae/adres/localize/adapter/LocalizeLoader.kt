package ae.adres.localize.adapter

import ae.adres.localize.domain.LocalizeStore
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/** Loader for local bundled keys (fallback when API/cache unavailable). */
typealias LocalBundleLoader = suspend () -> LocalizeStore?

/**
 * Default loader: returns empty store.
 * Android uses ResourcesLocalFallback (strings.xml, plurals) for per-key lookup instead.
 */
suspend fun defaultLocalLoader(): LocalizeStore? = LocalizeStore()

/** Parse JSON string to LocalizeStore (e.g. from assets). */
fun parseLocalBundleJson(jsonString: String): LocalizeStore? {
    return try {
        val json = JsonParser.parseString(jsonString).asJsonObject
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
            forms?.keySet()?.associateWith { forms.get(it).asString }?.let { pluralInner[key] = it }
        }
        plural[locale] = pluralInner
    }
    return LocalizeStore(simple = simple, plural = plural)
}
