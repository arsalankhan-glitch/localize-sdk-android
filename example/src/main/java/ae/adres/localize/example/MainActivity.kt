package ae.adres.localize.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ae.adres.localize.LocalizeSDK
import ae.adres.localize.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocalizeSDK.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        binding.btnRefresh.setOnClickListener { LocalizeSDK.refresh() }
        binding.btnEnglish.setOnClickListener {
            LocalizeSDK.setLocale("en")
        }
        binding.btnArabic.setOnClickListener {
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
        supportActionBar?.title = getString(R.string.app_name) + " (" + locale + ")"

        // Bundle-only and bundle keys: should resolve from app strings when API/cache is absent.
        binding.txtAppName.text = getString(R.string.app_name)
        binding.txtFromBundleOnly.text = getString(R.string.from_bundle_only)
        binding.txtBundleKey.text = getString(R.string.bundle_key)

        // Simple strings
        binding.txtWelcome.text = getString(R.string.welcome)
        binding.txtGreeting.text = getString(R.string.greeting, "User")

        // Plurals
        val count0 = 0
        val count1 = 1
        val count5 = 5
        binding.txtItemsCount0.text = resources.getQuantityString(R.plurals.items_count, count0, count0.toString())
        binding.txtItemsCount1.text = resources.getQuantityString(R.plurals.items_count, count1, count1.toString())
        binding.txtItemsCount5.text = resources.getQuantityString(R.plurals.items_count, count5, count5.toString())

        // Native resource ids can't represent a truly "missing key", but the SDK key API can.
        binding.txtSdkMissingKey.text = LocalizeSDK.getString("nonexistent_key_xyz")
    }
}
