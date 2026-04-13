package ae.adres.localize.adapter

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

/**
 * Per-key fallback using Android Resources (strings.xml, plurals).
 * Called when key is not found in API/cache store.
 * Uses standard framework methods: getString, getQuantityString.
 */
class ResourcesLocalFallback(
    private val context: Context,
    private val stringsFileName: String = "strings",
) {
    private val packageName: String = context.applicationContext.packageName

    fun getString(
        locale: String,
        key: String,
    ): String? {
        val res = resourcesForLocale(locale)
        val id = res.getIdentifier(key, "string", packageName)
        if (id == 0) return null
        return try {
            res.getString(id)
        } catch (_: Resources.NotFoundException) {
            null
        }
    }

    fun getPlural(
        locale: String,
        key: String,
        count: Int,
    ): String? {
        val res = resourcesForLocale(locale)
        val id = res.getIdentifier(key, "plurals", packageName)
        if (id == 0) return null
        return try {
            res.getQuantityString(id, count)
        } catch (_: Resources.NotFoundException) {
            null
        }
    }

    private fun resourcesForLocale(locale: String): Resources {
        val config =
            Configuration(context.resources.configuration).apply {
                setLocale(Locale.forLanguageTag(locale))
            }
        return context.createConfigurationContext(config).resources
    }
}
