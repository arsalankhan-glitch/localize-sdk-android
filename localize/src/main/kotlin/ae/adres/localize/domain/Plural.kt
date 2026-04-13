package ae.adres.localize.domain

/**
 * Plural form selection based on count.
 * CLDR rules: https://unicode-org.github.io/cldr-staging/charts/43/supplemental/language_plural_rules.html
 * Common forms: zero, one, two, few, many, other
 */
fun selectPluralForm(
    locale: String,
    count: Int,
): String {
    val lang = locale.split("-", "_").firstOrNull()?.lowercase() ?: locale
    return when (lang) {
        "ar" -> arabicPlural(count)
        "ru", "uk", "pl" -> slavicPlural(count)
        "fr" -> frenchPlural(count)
        else -> defaultPlural(count)
    }
}

private fun defaultPlural(count: Int) = if (count == 1) "one" else "other"

private fun arabicPlural(count: Int): String {
    if (count == 0) return "zero"
    if (count == 1) return "one"
    if (count == 2) return "two"
    if (count in 3..10) return "few"
    if (count in 11..99) return "many"
    return "other"
}

private fun slavicPlural(count: Int): String {
    if (count % 10 == 1 && count % 100 != 11) return "one"
    if (count % 10 in 2..4 && (count % 100 < 10 || count % 100 >= 20)) return "few"
    if (count % 10 == 0 || (count % 10 in 5..9) || (count % 100 in 11..19)) return "many"
    return "other"
}

private fun frenchPlural(count: Int) = if (count == 0 || count == 1) "one" else "other"
