package ae.adres.localize.usecase

import ae.adres.localize.LocalizeConfig
import ae.adres.localize.adapter.LocalBundleLoader
import ae.adres.localize.adapter.LocalizeCache
import ae.adres.localize.adapter.LocalizeFetcher
import ae.adres.localize.adapter.ResourcesLocalFallback
import ae.adres.localize.domain.LocalizeResolver
import ae.adres.localize.domain.LocalizeStore
import ae.adres.localize.domain.interpolateTemplate
import ae.adres.localize.domain.selectPluralForm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Internal SDK implementation. Use LocalizeSDK for the public API. */
class LocalizeSDKImpl(
    private val config: LocalizeConfig,
    private val fetcher: LocalizeFetcher,
    private val cache: LocalizeCache,
    private val localLoader: LocalBundleLoader,
    private val localFallback: ResourcesLocalFallback? = null,
) {
    private var store: LocalizeStore = LocalizeStore()
    private var initialized = false
    private var fetchInProgress = false
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var locale: String = "en"

    fun setLocaleTo(newLocale: String) {
        locale = newLocale
        scope.launch {
            val loaded = cache.load(newLocale)
            if (loaded != null) {
                store = loaded
            } else {
                val local = localLoader()
                if (local != null && !local.isEmpty) {
                    val newSimple = store.simple.toMutableMap()
                    val newPlural = store.plural.toMutableMap()
                    local.simple[newLocale]?.let { newSimple[newLocale] = it }
                    local.plural[newLocale]?.let { newPlural[newLocale] = it }
                    store = LocalizeStore(simple = newSimple, plural = newPlural)
                }
            }
            withContext(Dispatchers.Main) {
                config.onKeysUpdated?.invoke()
            }
        }
    }

    suspend fun initStore() {
        mutex.withLock {
            if (initialized) return
            initialized = true
        }
        val fetched = fetcher.fetch()
        if (fetched != null) {
            cache.save(fetched)
            store = extractLocale(fetched, locale)
            withContext(Dispatchers.Main) { config.onKeysUpdated?.invoke() }
            return
        }
        val cached = cache.load(locale)
        val local = localLoader()
        store = LocalizeResolver.resolve(apiOrCache = cached, local = local)
    }

    private fun extractLocale(
        full: LocalizeStore,
        targetLocale: String,
    ): LocalizeStore {
        val simple = mutableMapOf<String, Map<String, String>>()
        val plural = mutableMapOf<String, Map<String, Map<String, String>>>()
        full.simple[targetLocale]?.let { simple[targetLocale] = it }
        full.plural[targetLocale]?.let { plural[targetLocale] = it }
        config.fallbackLocale?.takeIf { it != targetLocale }?.let { fallback ->
            full.simple[fallback]?.let { simple[fallback] = it }
            full.plural[fallback]?.let { plural[fallback] = it }
        }
        return LocalizeStore(simple = simple, plural = plural)
    }

    fun refresh() {
        scope.launch {
            mutex.withLock {
                if (fetchInProgress) return@launch
                fetchInProgress = true
            }
            performRefresh()
        }
    }

    private suspend fun performRefresh() {
        try {
            val fetched = fetcher.fetch()
            if (fetched != null) {
                cache.save(fetched)
                store = extractLocale(fetched, locale)
            }
        } finally {
            mutex.withLock { fetchInProgress = false }
            withContext(Dispatchers.Main) { config.onKeysUpdated?.invoke() }
        }
    }

    fun getString(
        key: String,
        args: List<Any>? = null,
    ): String {
        val value = resolveStringValue(key)
        if (value == null) return key
        return if (args != null && args.isNotEmpty()) interpolateTemplate(value, args) else value
    }

    fun getPlural(
        key: String,
        count: Int,
    ): String {
        val fallbackValue = resolvePluralValue(key, count)
        return fallbackValue ?: key
    }

    /**
     * Template-only lookup used by the native `Resources` proxy.
     *
     * Important: this must be *SDK-store-only*, so that when the SDK misses a key
     * the proxy can delegate to Android resources and preserve native formatting
     * semantics for `getString(..., formatArgs)` and `getQuantityString(..., formatArgs)`.
     */
    internal fun getStringTemplate(key: String): String? = resolveStringTemplateFromStore(key)

    internal fun getPluralTemplate(
        key: String,
        count: Int,
    ): String? = resolvePluralTemplateFromStore(key, count)

    private fun resolveStringValue(key: String): String? {
        var value = store.simple[locale]?.get(key)
        if (value == null && config.fallbackLocale != null) {
            value = store.simple[config.fallbackLocale!!]?.get(key)
        }
        if (value == null) {
            value = localFallback?.getString(locale, key)
        }
        if (value == null && config.fallbackLocale != null) {
            value = localFallback?.getString(config.fallbackLocale!!, key)
        }
        return value
    }

    private fun resolveStringTemplateFromStore(key: String): String? {
        var value = store.simple[locale]?.get(key)
        if (value == null && config.fallbackLocale != null) {
            value = store.simple[config.fallbackLocale!!]?.get(key)
        }
        return value
    }

    private fun resolvePluralTemplateValue(
        key: String,
        count: Int,
    ): String? {
        val form = selectPluralForm(locale, count)
        var pluralMap = store.plural[locale]?.get(key)
        if (pluralMap == null && config.fallbackLocale != null) {
            pluralMap = store.plural[config.fallbackLocale!!]?.get(key)
        }
        if (pluralMap != null) {
            return pluralMap[form] ?: pluralMap["other"] ?: pluralMap.values.firstOrNull()
        }
        return localFallback?.getPlural(locale, key, count)
            ?: config.fallbackLocale?.let { localFallback?.getPlural(it, key, count) }
    }

    private fun resolvePluralTemplateFromStore(
        key: String,
        count: Int,
    ): String? {
        val form = selectPluralForm(locale, count)
        var pluralMap = store.plural[locale]?.get(key)
        if (pluralMap == null && config.fallbackLocale != null) {
            pluralMap = store.plural[config.fallbackLocale!!]?.get(key)
        }
        if (pluralMap == null) return null
        return pluralMap[form] ?: pluralMap["other"] ?: pluralMap.values.firstOrNull()
    }

    private fun resolvePluralValue(
        key: String,
        count: Int,
    ): String? {
        val template = resolvePluralTemplateValue(key, count) ?: return null
        return interpolateTemplate(template, listOf(count))
    }
}
