package ae.adres.localize.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizeResolverTest {

    @Test
    fun apiOrCacheWins() {
        val api = LocalizeStore(
            simple = mapOf("en" to mapOf("k" to "from-api")),
            plural = emptyMap()
        )
        val local = LocalizeStore(
            simple = mapOf("en" to mapOf("k" to "from-local")),
            plural = emptyMap()
        )
        val result = LocalizeResolver.resolve(apiOrCache = api, local = local)
        assertEquals("from-api", result.simple["en"]?.get("k"))
    }

    @Test
    fun localFallbackWhenApiEmpty() {
        val local = LocalizeStore(
            simple = mapOf("en" to mapOf("k" to "from-local")),
            plural = emptyMap()
        )
        val result = LocalizeResolver.resolve(apiOrCache = null, local = local)
        assertEquals("from-local", result.simple["en"]?.get("k"))
    }

    @Test
    fun bothEmpty() {
        val result = LocalizeResolver.resolve(apiOrCache = null, local = null)
        assertTrue(result.isEmpty)
    }
}
