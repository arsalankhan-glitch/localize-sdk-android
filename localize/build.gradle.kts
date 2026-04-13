plugins {
    alias(localizeSdk.plugins.android.library)
    alias(localizeSdk.plugins.kotlin.android)
}

group = "ae.adres"
version = "0.1.0"

android {
    namespace = "ae.adres.localize"
    buildToolsVersion = "35.0.0"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
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
    implementation(localizeSdk.okhttp)
    implementation(localizeSdk.gson)
    implementation(localizeSdk.kotlinx.coroutines.android)

    testImplementation(localizeSdk.junit)
    testImplementation(localizeSdk.kotlinx.coroutines.test)
}
