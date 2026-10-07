# Localize SDK for Android

Kotlin SDK that fetches translations from the Localize API and falls back to your app's `strings.xml` when offline. It can also intercept native `getString` calls.

Example app: [`example/`](example/). Run it with `./gradlew :example:installDebug -PLOCALIZE_EXAMPLE_API_KEY=pk_xxx`, or set `LOCALIZE_EXAMPLE_API_KEY` in `~/.gradle/gradle.properties` or as an environment variable.

## Requirements

- `minSdk` 26 or higher
- Built with Android Gradle Plugin 8.7.2 and Kotlin 2.0.21

## Installation

The SDK is published through [JitPack](https://jitpack.io/#arsalankhan-glitch/localize-sdk-android). No account or token is needed.

Add the JitPack repository in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Add the dependency in `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.arsalankhan-glitch:localize-sdk-android:0.1.0")
}
```

The SDK declares the `INTERNET` permission itself, so you don't need to add it to your manifest.

## Publishing a new version

Push a git tag with the version number. JitPack builds that tag the first time someone requests it:

```bash
git tag 0.2.0
git push origin 0.2.0
```

To test a build locally, run `./gradlew :localize:publishReleasePublicationToMavenLocal` and add `mavenLocal()` to your app's repositories.

## Setup

Call `configure` once in your `Application.onCreate`:

```kotlin
import ae.adres.localize.LocalizeSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LocalizeSDK.configure(
            context = this,
            apiKey = "pk_xxx",
            fallbackLocale = "en",
            onKeysUpdated = { /* reload UI */ },
            onReady = { /* SDK is ready */ }
        )
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocalizeSDK.wrapContext(base))
    }
}
```

Wrap each `Activity`'s base context to intercept native `getString` calls:

```kotlin
class MainActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocalizeSDK.wrapContext(newBase))
    }
}
```

## Usage

```kotlin
// Simple string
val label = LocalizeSDK.getString("welcome_message")

// Interpolated string
val greeting = LocalizeSDK.getString("greeting", args = listOf("John"))

// Plural
val count = LocalizeSDK.getPlural("items_count", 5)

// Switch locale
LocalizeSDK.setLocale("ar")

// Refresh from API
LocalizeSDK.refresh()
```

## Configuration options

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `apiKey` | `String` | — | Project API key (required) |
| `fallbackLocale` | `String?` | `null` | Locale to use when key is missing |
| `baseUrl` | `String?` | `"https://localize-api.adres.ae"` | API host |
| `stringsFileName` | `String` | `"strings"` | Name of the strings XML resource file |
| `timeoutSeconds` | `Int` | `10` | Network request timeout |
| `enableLogging` | `Boolean` | `true` | Print debug logs |
| `onKeysUpdated` | `() -> Unit` | `null` | Called after each successful refresh |
| `onReady` | `() -> Unit` | `null` | Called when initial load completes |

## How it works

1. On `configure`, the SDK fetches all translations from the API (every locale) and caches them on disk.
2. If the fetch fails, the SDK uses the cached translations for the current locale.
3. If the API is unreachable, it falls back to your app's `strings.xml`.
4. Call `refresh()` at any time to pull the latest translations in the background.
5. Call `setLocale("ar")` to switch locale. The SDK reads that locale from the cache, with no network request.

## License

[MIT](LICENSE)
