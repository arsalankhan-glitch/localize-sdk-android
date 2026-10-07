package ae.adres.localize.domain

/**
 * Interpolates SDK templates in a way that matches the existing LocalizeSDKImpl behavior.
 *
 * Android templates typically use `%s` / `%d`, but the SDK also supports `%@` (from the shared
 * API format) by replacing sequentially in the order placeholders appear.
 *
 * It also supports positional arguments like `%1$s`, `%2$d`, etc.
 */
internal fun interpolateTemplate(
    template: String,
    args: List<Any?>,
): String {
    val regex = Regex("""%(?:(\d+)\$)?([sd@])""")
    val matches = regex.findAll(template).toList()
    if (matches.isEmpty() || args.isEmpty()) return template

    var nextSequentialIdx = 0
    val matchesWithIndices =
        matches.map { match ->
            val indexStr = match.groupValues[1]
            val argIdx =
                if (indexStr.isNotEmpty()) {
                    indexStr.toIntOrNull()?.minus(1) ?: -1
                } else {
                    nextSequentialIdx++
                }
            match to argIdx
        }

    var result = template
    matchesWithIndices.reversed().forEach { (match, argIdx) ->
        if (argIdx in args.indices) {
            result = result.replaceRange(match.range, args[argIdx]?.toString() ?: "")
        }
    }
    return  result.replace("%%", "%")
}
