package ae.adres.localize.domain

/**
 * Interpolates SDK templates in a way that matches the existing LocalizeSDKImpl behavior.
 *
 * Android templates typically use `%s` / `%d`, but the SDK also supports `%@` (from the shared
 * API format) by replacing sequentially in the order placeholders appear.
 */
internal fun interpolateTemplate(
    template: String,
    args: List<Any>,
): String {
    val matches = Regex("%[sd@]").findAll(template).toList()
    if (matches.isEmpty() || args.isEmpty()) return template

    var result = template
    matches.reversed().forEachIndexed { i, match ->
        val idx = matches.size - 1 - i
        if (idx < args.size) {
            result = result.replaceRange(match.range, args[idx].toString())
        }
    }
    return result
}
