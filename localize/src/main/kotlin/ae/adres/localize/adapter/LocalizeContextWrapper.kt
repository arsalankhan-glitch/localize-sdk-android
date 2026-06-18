package ae.adres.localize.adapter

import ae.adres.localize.LocalizeSDK
import ae.adres.localize.domain.interpolateTemplate
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import android.util.DisplayMetrics
import java.util.Locale

/**
 * Context wrapper that injects SDK-first localization for native Resources APIs.
 */
class LocalizeContextWrapper(
    base: Context,
) : ContextWrapper(base) {
    private val proxyResources: Resources by lazy {
        LocalizeResourcesProxy(baseContext.resources)
    }

    override fun getResources(): Resources = proxyResources

    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        val baseConfig = baseContext.resources.configuration
        val newConfig = Configuration(baseConfig)

        val overrideLocales = overrideConfiguration.locales
        val overrideLocaleList =
            if (overrideLocales.isEmpty) {
                LocaleList(Locale.getDefault())
            } else {
                overrideLocales
            }
        newConfig.setLocales(overrideLocaleList)

        val ctx = baseContext.createConfigurationContext(newConfig)
        return LocalizeContextWrapper(ctx)
    }
}

class LocalizeResourcesProxy(
    private val base: Resources,
) : @Suppress("DEPRECATION")
    Resources(base.assets, base.displayMetrics, base.configuration) {
    fun getString(key: String): String? = LocalizeSDK.resolveStringTemplateForResourceKey(key)

    override fun getConfiguration(): Configuration = base.configuration

    override fun getDisplayMetrics(): DisplayMetrics? = base.displayMetrics

    override fun getString(id: Int): String {
        val key = safeEntryName(id) ?: return base.getString(id)
        return LocalizeSDK.resolveStringTemplateForResourceKey(key) ?: base.getString(id)
    }

    override fun getString(
        id: Int,
        vararg formatArgs: Any?,
    ): String {
        val key = safeEntryName(id) ?: return base.getString(id, *formatArgs)
        val template =
            LocalizeSDK.resolveStringTemplateForResourceKey(key) ?: return base.getString(
                id,
                *formatArgs,
            )
        return interpolateTemplate(template, formatArgs.toList())
    }

    override fun getQuantityString(
        id: Int,
        quantity: Int,
    ): String {
        val key = safeEntryName(id) ?: return base.getQuantityString(id, quantity)
        val template =
            LocalizeSDK.resolvePluralTemplateForResourceKey(key, quantity)
                ?: return base.getQuantityString(id, quantity)
        return interpolateTemplate(template, listOf(quantity))
    }

    override fun getQuantityString(
        id: Int,
        quantity: Int,
        vararg formatArgs: Any?,
    ): String {
        val key = safeEntryName(id) ?: return base.getQuantityString(id, quantity, *formatArgs)
        val template =
            LocalizeSDK.resolvePluralTemplateForResourceKey(key, quantity)
                ?: return base.getQuantityString(id, quantity, *formatArgs)

        // Match Android behavior where `quantity` participates in formatting even when you pass extra args.
        return interpolateTemplate(template, listOf(quantity) + formatArgs.toList())
    }

    private fun safeEntryName(id: Int): String? =
        try {
            base.getResourceEntryName(id)
        } catch (_: Resources.NotFoundException) {
            null
        }
}
