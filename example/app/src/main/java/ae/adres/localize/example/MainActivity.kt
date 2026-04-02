package ae.adres.localize.example

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ae.adres.localize.LocalizeSDK

class MainActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocalizeSDK.wrapContext(newBase))
    }

    private lateinit var txtAppName: TextView
    private lateinit var txtFromBundleOnly: TextView
    private lateinit var txtBundleKey: TextView
    private lateinit var txtWelcome: TextView
    private lateinit var txtGreeting: TextView
    private lateinit var txtItemsCount0: TextView
    private lateinit var txtItemsCount1: TextView
    private lateinit var txtItemsCount5: TextView
    private lateinit var txtSdkMissingKey: TextView

    private lateinit var btnRefresh: Button
    private lateinit var btnEnglish: Button
    private lateinit var btnArabic: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtAppName = findViewById(R.id.txtAppName)
        txtFromBundleOnly = findViewById(R.id.txtFromBundleOnly)
        txtBundleKey = findViewById(R.id.txtBundleKey)
        txtWelcome = findViewById(R.id.txtWelcome)
        txtGreeting = findViewById(R.id.txtGreeting)
        txtItemsCount0 = findViewById(R.id.txtItemsCount0)
        txtItemsCount1 = findViewById(R.id.txtItemsCount1)
        txtItemsCount5 = findViewById(R.id.txtItemsCount5)
        txtSdkMissingKey = findViewById(R.id.txtSdkMissingKey)

        btnRefresh = findViewById(R.id.btnRefresh)
        btnEnglish = findViewById(R.id.btnEnglish)
        btnArabic = findViewById(R.id.btnArabic)

        btnRefresh.setOnClickListener { LocalizeSDK.refresh() }
        btnEnglish.setOnClickListener {
            LocalizeSDK.setLocale("en")
        }
        btnArabic.setOnClickListener {
            LocalizeSDK.setLocale("ar")
        }

        // Configure once at startup. Using an empty API key will still work via bundled strings.
        LocalizeSDK.configure(
            context = this,
            apiKey = BuildConfig.LOCALIZE_EXAMPLE_API_KEY,
            fallbackLocale = "en",
            enableLogging = false,
            onKeysUpdated = { runOnUiThread { updateUi() } },
            onReady = { runOnUiThread { updateUi() } }
        )
    }

    private fun updateUi() {
        val locale = LocalizeSDK.getLocale()
        title = getString(R.string.app_name) + " (" + locale + ")"

        // Bundle-only and bundle keys: should resolve from app strings when API/cache is absent.
        txtAppName.text = getString(R.string.app_name)
        txtFromBundleOnly.text = getString(R.string.from_bundle_only)
        txtBundleKey.text = getString(R.string.bundle_key)

        // Simple strings
        txtWelcome.text = getString(R.string.welcome)
        txtGreeting.text = getString(R.string.greeting, "User")

        // Plurals
        val count0 = 0
        val count1 = 1
        val count5 = 5
        txtItemsCount0.text = resources.getQuantityString(R.plurals.items_count, count0, count0.toString())
        txtItemsCount1.text = resources.getQuantityString(R.plurals.items_count, count1, count1.toString())
        txtItemsCount5.text = resources.getQuantityString(R.plurals.items_count, count5, count5.toString())

        // Native resource ids can't represent a truly "missing key", but the SDK key API can.
        txtSdkMissingKey.text = LocalizeSDK.getString("nonexistent_key_xyz")
    }
}

