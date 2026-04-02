package ae.adres.localize.example

import ae.adres.localize.LocalizeSDK
import ae.adres.localize.adapter.LocalizeCache
import ae.adres.localize.adapter.LocalizeFetcher
import ae.adres.localize.domain.LocalizeStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NativeSignatureParityTest {

    private val enStore = LocalizeStore(
        simple = mapOf(
            "en" to mapOf(
                "add_company_to_dari" to "SDK add_company_to_dari",
                "greeting" to "SDK Hello, %s!"
            )
        ),
        plural = mapOf(
            "en" to mapOf(
                "years" to mapOf(
                    "one" to "%s year (SDK)",
                    "other" to "%s years (SDK %s)"
                ),
                "items_count" to mapOf(
                    "zero" to "SDK no items",
                    "one" to "%s item (SDK)",
                    "other" to "%s items (SDK)"
                )
            )
        )
    )

    @Before
    fun resetSdk() {
        LocalizeSDK.resetForTesting()
    }

    @Test
    fun nativeGetStringAndQuantityStringAreSdkFirst() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val readyLatch = CountDownLatch(1)
        LocalizeSDK.configure(
            context = context,
            apiKey = "test",
            fallbackLocale = "en",
            enableLogging = false,
            fetcher = StubFetcher(enStore),
            cache = StubCache(enStore),
            localLoader = { null },
            onReady = { readyLatch.countDown() }
        )

        assertTrue("LocalizeSDK did not become ready in time", readyLatch.await(5, TimeUnit.SECONDS))

        val wrapped = LocalizeSDK.wrapContext(context)

        // getString(id, formatArgs...)
        assertEquals(
            "SDK Hello, User!",
            wrapped.getString(R.string.greeting, "User")
        )

        // getString(id) without args
        assertEquals(
            "SDK add_company_to_dari",
            wrapped.getString(R.string.add_company_to_dari)
        )

        // getQuantityString(id, quantity) without explicit format args
        assertEquals(
            "1 year (SDK)",
            wrapped.resources.getQuantityString(R.plurals.years, 1)
        )

        // getQuantityString(id, quantity, formatArgs...) with extra format args
        assertEquals(
            "2 years (SDK X)",
            wrapped.resources.getQuantityString(R.plurals.years, 2, "X")
        )

        // Miss in SDK store should fall back to bundle value for bundle_key.
        assertEquals(
            "bundle_key (EN - bundle)",
            wrapped.getString(R.string.bundle_key)
        )
    }
}

private class StubFetcher(private val store: LocalizeStore) : LocalizeFetcher {
    override suspend fun fetch(): LocalizeStore? = store
}

private class StubCache(private val store: LocalizeStore) : LocalizeCache {
    override suspend fun load(locale: String): LocalizeStore? = store
    override suspend fun save(store: LocalizeStore) { /* no-op for example */ }
}

