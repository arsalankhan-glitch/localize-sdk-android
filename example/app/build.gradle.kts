plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ae.adres.localize.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "ae.adres.localize.example"
        minSdk = 21
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey =
            (findProperty("LOCALIZE_EXAMPLE_API_KEY") as String?) ?: System.getenv("LOCALIZE_EXAMPLE_API_KEY") ?: ""
        buildConfigField("String", "LOCALIZE_EXAMPLE_API_KEY", "\"$apiKey\"")
    }

    buildFeatures {
        viewBinding = false
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.13.1")

    implementation(project(":android_localize_sdk"))

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

