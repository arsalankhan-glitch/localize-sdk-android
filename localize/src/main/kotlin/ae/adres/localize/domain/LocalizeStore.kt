package ae.adres.localize.domain

/**
 * In-memory storage structure for O(1) lookup.
 * simple: Map<Locale, Map<Key, String>>
 * plural: Map<Locale, Map<Key, Map<PluralForm, String>>>
 */
data class LocalizeStore(
    val simple: Map<String, Map<String, String>> = emptyMap(),
    val plural: Map<String, Map<String, Map<String, String>>> = emptyMap(),
) {
    val isEmpty: Boolean get() = simple.isEmpty() && plural.isEmpty()

    /** Deep copy for immutability when replacing store. */
    fun deepCopy(): LocalizeStore {
        val newSimple = simple.mapValues { (_, v) -> v.toMap() }
        val newPlural =
            plural.mapValues { (_, keys) ->
                keys.mapValues { (_, forms) -> forms.toMap() }
            }
        return LocalizeStore(simple = newSimple, plural = newPlural)
    }
}
