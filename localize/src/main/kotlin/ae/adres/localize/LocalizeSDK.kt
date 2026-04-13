package ae.adres.localize

import ae.adres.localize.adapter.FileLocalizeCache
import ae.adres.localize.adapter.LocalBundleLoader
import ae.adres.localize.adapter.LocalizeCache
import ae.adres.localize.adapter.LocalizeContextWrapper
import ae.adres.localize.adapter.LocalizeFetcher
import ae.adres.localize.adapter.OkHttpLocalizeFetcher
import ae.adres.localize.adapter.ResourcesLocalFallback
import ae.adres.localize.adapter.defaultLocalLoader
import ae.adres.localize.usecase.LocalizeSDKImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

/**
 * Public API for the Localize Android SDK.
 *
 * Usage:
 * ```kotlin
 * LocalizeSDK.configure(
 *     context = context,
 *     apiKey = "pk_xxx",
 *     onKeysUpdated = { refreshUI() },
 *     fallbackLocale = "en"
 * )
 * // In view:
 * Text(LocalizeSDK.getString("welcome_message"))
 * Text(LocalizeSDK.getString("greeting", args = listOf("John")))
 * Text(LocalizeSDK.getPlural("items_count", 5))
 * ```
 */
object LocalizeSDK {
    @Volatile
    private var instance: LocalizeSDKImpl? = null

    private var contextRef: WeakReference<android.content.Context>? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Configure the SDK. Call once at app startup.
     * Requires Context for cache directory.
     * Init runs in background; [onReady] is invoked on Main when done.
     */
    fun configure(
        context: android.content.Context,
        apiKey: String,
        platform: String = "android",
        baseUrl: String? = null,
        onKeysUpdated: (() -> Unit)? = null,
        onReady: (() -> Unit)? = null,
        fallbackLocale: String? = null,
        timeoutSeconds: Int = 10,
        localLoader: LocalBundleLoader? = null,
        enableLogging: Boolean = true,
        stringsFileName: String = "strings",
        fetcher: LocalizeFetcher? = null,
        cache: LocalizeCache? = null,
        localFallback: ResourcesLocalFallback? = null,
    ) {
        val config =
            LocalizeConfig(
                apiKey = apiKey,
                platform = platform,
                baseUrl = baseUrl ?: "https://localize-dev-api.adres.ae",
                onKeysUpdated = onKeysUpdated,
                fallbackLocale = fallbackLocale,
                timeoutSeconds = timeoutSeconds,
                enableLogging = enableLogging,
                stringsFileName = stringsFileName,
            )
        contextRef = WeakReference(context.applicationContext)
        val appContext = context.applicationContext
        val cacheDir = appContext.cacheDir
        val fallback = localFallback ?: ResourcesLocalFallback(appContext, stringsFileName)
        val impl =
            LocalizeSDKImpl(
                config = config,
                fetcher = fetcher ?: OkHttpLocalizeFetcher(config),
                cache = cache ?: FileLocalizeCache(config, cacheDir),
                localLoader = localLoader ?: { defaultLocalLoader() },
                localFallback = fallback,
            )
        instance = impl
        scope.launch {
            withContext(Dispatchers.IO) { impl.initStore() }
            onReady?.invoke()
        }
    }

    /** Set the current locale. Loads from cache in background; onKeysUpdated when done. */
    fun setLocale(value: String) {
        instance?.setLocaleTo(value)
    }

    /**
     * Wrap a Context so native getString/getQuantityString calls become SDK-aware.
     * Use in Application/Activity attachBaseContext.
     */
    fun wrapContext(baseContext: android.content.Context): android.content.Context = LocalizeContextWrapper(baseContext)

    /** Get the current locale. */
    fun getLocale(): String = instance?.locale ?: "en"

    /** Refresh keys from API. Runs in background. Invokes onKeysUpdated when done. */
    fun refresh() {
        instance?.refresh()
    }

    /** Get a simple string. */
    fun getString(
        key: String,
        args: List<Any>? = null,
    ): String = instance?.getString(key, args) ?: key

    /** Get a plural string. */
    fun getPlural(
        key: String,
        count: Int,
    ): String = instance?.getPlural(key, count) ?: key

    /** Check if SDK is configured. */
    fun isConfigured(): Boolean = instance != null

    /** Reset instance. For testing only. */
    fun resetForTesting() {
        instance = null
        contextRef = null
    }

    internal fun resolveStringTemplateForResourceKey(key: String): String? = instance?.getStringTemplate(key)

    internal fun resolvePluralTemplateForResourceKey(
        key: String,
        quantity: Int,
    ): String? = instance?.getPluralTemplate(key, quantity)
}
