package ae.adres.localize.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizeStoreTest {
    @Test
    fun emptyStore() {
        val store = LocalizeStore()
        assertTrue(store.isEmpty)
        assertTrue(store.simple.isEmpty())
        assertTrue(store.plural.isEmpty())
    }

    @Test
    fun nonEmptyStore() {
        val store =
            LocalizeStore(
                simple = mapOf("en" to mapOf("welcome" to "Welcome!")),
                plural = emptyMap(),
            )
        assertTrue(!store.isEmpty)
        assertEquals("Welcome!", store.simple["en"]?.get("welcome"))
    }

    @Test
    fun deepCopy() {
        val store =
            LocalizeStore(
                simple = mapOf("en" to mapOf("k" to "v")),
                plural = mapOf("en" to mapOf("items" to mapOf("one" to "1", "other" to "many"))),
            )
        val copy = store.deepCopy()
        assertEquals("v", copy.simple["en"]?.get("k"))
        assertEquals("1", copy.plural["en"]?.get("items")?.get("one"))
    }
}
