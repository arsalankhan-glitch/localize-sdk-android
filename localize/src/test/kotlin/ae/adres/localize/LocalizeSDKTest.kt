package ae.adres.localize

import ae.adres.localize.adapter.LocalizeCache
import ae.adres.localize.adapter.LocalizeFetcher
import ae.adres.localize.domain.LocalizeStore
import ae.adres.localize.domain.interpolateTemplate
import ae.adres.localize.usecase.LocalizeSDKImpl
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class LocalizeSDKTest {

    @Before
    @After
    fun reset() {
        LocalizeSDK.resetForTesting()
    }

    @Test
    fun unconfiguredReturnsKey() {
        assertEquals("any_key", LocalizeSDK.getString("any_key"))
        assertEquals("items", LocalizeSDK.getPlural("items", 5))
    }

    @Test
    fun configureAndGetString() = runTest {
        val store = LocalizeStore(
            simple = mapOf("en" to mapOf("welcome" to "Welcome!")),
            plural = emptyMap()
        )
        val config = LocalizeConfig(apiKey = "test", platform = "android")
        val cache = MockCache(store)
        val fetcher = MockFetcher(null)
        val impl = LocalizeSDKImpl(
            config = config,
            fetcher = fetcher,
            cache = cache,
            localLoader = { null }
        )
        impl.initStore()

        impl.locale = "en"
        assertEquals("Welcome!", impl.getString("welcome"))
        assertEquals("missing", impl.getString("missing"))
    }

    @Test
    fun getPlural() = runTest {
        val store = LocalizeStore(
            simple = emptyMap(),
            plural = mapOf("en" to mapOf("items" to mapOf("one" to "1 item", "other" to "%d items")))
        )
        val config = LocalizeConfig(apiKey = "test", platform = "android")
        val cache = MockCache(store)
        val impl = LocalizeSDKImpl(
            config = config,
            fetcher = MockFetcher(null),
            cache = cache,
            localLoader = { null }
        )
        impl.initStore()

        impl.locale = "en"
        assertEquals("1 item", impl.getPlural("items", 1))
        assertEquals("5 items", impl.getPlural("items", 5))
    }

    @Test
    fun interpolation() = runTest {
        val store = LocalizeStore(
            simple = mapOf("en" to mapOf("greeting" to "Hello, %s!")),
            plural = emptyMap()
        )
        val config = LocalizeConfig(apiKey = "test", platform = "android")
        val cache = MockCache(store)
        val impl = LocalizeSDKImpl(
            config = config,
            fetcher = MockFetcher(null),
            cache = cache,
            localLoader = { null }
        )
        impl.initStore()

        impl.locale = "en"
        assertEquals("Hello, John!", impl.getString("greeting", args = listOf("John")))
    }

    @Test
    fun getStringTemplateReturnsRawValue() = runTest {
        val store = LocalizeStore(
            simple = mapOf("en" to mapOf("greeting" to "Hello, %s!")),
            plural = emptyMap()
        )
        val impl = LocalizeSDKImpl(
            config = LocalizeConfig(apiKey = "test", platform = "android"),
            fetcher = MockFetcher(null),
            cache = MockCache(store),
            localLoader = { null }
        )
        impl.initStore()

        impl.locale = "en"
        assertEquals("Hello, %s!", impl.getStringTemplate("greeting"))
        assertEquals(null, impl.getStringTemplate("missing"))
    }

    @Test
    fun getPluralTemplateReturnsFormWithoutInterpolation() = runTest {
        val store = LocalizeStore(
            simple = emptyMap(),
            plural = mapOf("en" to mapOf("years" to mapOf("one" to "%d year", "other" to "%d years")))
        )
        val impl = LocalizeSDKImpl(
            config = LocalizeConfig(apiKey = "test", platform = "android"),
            fetcher = MockFetcher(null),
            cache = MockCache(store),
            localLoader = { null }
        )
        impl.initStore()

        impl.locale = "en"
        assertEquals("%d year", impl.getPluralTemplate("years", 1))
        assertEquals("%d years", impl.getPluralTemplate("years", 2))
    }

    @Test
    fun interpolateTemplateReplacesAllPlaceholdersInOrder() {
        assertEquals(
            "Hello John, you have 3 messages",
            interpolateTemplate(
                "Hello %s, you have %d messages",
                listOf("John", 3)
            )
        )
    }

    @Test
    fun interpolateTemplateSupportsAtSymbolPlaceholder() {
        assertEquals("Value: X", interpolateTemplate("Value: %@ ", listOf("X")).trim())
    }
}

private class MockCache(private var store: LocalizeStore?) : LocalizeCache {
    override suspend fun load(locale: String): LocalizeStore? = store
    override suspend fun save(s: LocalizeStore) { store = s }
}

private class MockFetcher(private val result: LocalizeStore?) : LocalizeFetcher {
    override suspend fun fetch(): LocalizeStore? = result
}
