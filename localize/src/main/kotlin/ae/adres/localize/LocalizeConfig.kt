package ae.adres.localize

/**
 * Configuration for the Localize SDK.
 */
data class LocalizeConfig(
    val apiKey: String,
    val platform: String = "android",
    val baseUrl: String = "https://localize-dev-api.adres.ae",
    val onKeysUpdated: (() -> Unit)? = null,
    val fallbackLocale: String? = null,
    val timeoutSeconds: Int = 10,
    val enableLogging: Boolean = true,
    /** Base name for strings resource file (default "strings" -> strings.xml). */
    val stringsFileName: String = "strings",
) {
    val normalizedBaseUrl: String = baseUrl.trimEnd('/')
}
