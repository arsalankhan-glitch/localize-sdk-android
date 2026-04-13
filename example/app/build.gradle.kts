plugins {
    alias(localizeSdk.plugins.android.application)
    alias(localizeSdk.plugins.kotlin.android)
}

android {
    namespace = "ae.adres.localize.example"
    buildToolsVersion = "35.0.0"
    compileSdk = 36

    defaultConfig {
        applicationId = "ae.adres.localize.example"
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey =
            (findProperty("LOCALIZE_EXAMPLE_API_KEY") as String?) ?: System.getenv("LOCALIZE_EXAMPLE_API_KEY") ?: ""
        buildConfigField("String", "LOCALIZE_EXAMPLE_API_KEY", "\"$apiKey\"")
    }

    buildFeatures {
        dataBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(project(":android_localize_sdk"))

    implementation(localizeSdk.androidx.appcompat)
    implementation(localizeSdk.androidx.core.ktx)
    implementation(localizeSdk.material)


    androidTestImplementation(localizeSdk.androidx.test.ext.junit)
    androidTestImplementation(localizeSdk.androidx.test.core)
    androidTestImplementation(localizeSdk.androidx.test.runner)
    androidTestImplementation(localizeSdk.androidx.test.espresso.core)
}
