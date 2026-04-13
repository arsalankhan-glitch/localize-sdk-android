package ae.adres.localize.domain

/**
 * Resolution order:
 * 1. API response (stored in cache)
 * 2. Cached API data
 * 3. Local bundled keys
 *
 * API data always overwrites cache. Local keys are fallback only when
 * API/cache are unavailable.
 */
object LocalizeResolver {
    /** Resolve which store to use. Prefer apiOrCache over local. */
    fun resolve(
        apiOrCache: LocalizeStore?,
        local: LocalizeStore?,
    ): LocalizeStore {
        if (apiOrCache != null && !apiOrCache.isEmpty) return apiOrCache
        if (local != null && !local.isEmpty) return local
        return apiOrCache ?: local ?: LocalizeStore()
    }
}
