plugins {
    alias(localizeSdk.plugins.android.library)
    alias(localizeSdk.plugins.kotlin.android)
    `maven-publish`
}

group = "ae.adres"
version = (findProperty("sdkVersion") as String?) ?: "0.1.0"

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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "ae.adres"
            artifactId = "localize-sdk"
            version = project.version.toString()
            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/arsalankhan-glitch/localize-sdk")
            credentials {
                username = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
                password = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencies {
    implementation(localizeSdk.okhttp)
    implementation(localizeSdk.gson)
    implementation(localizeSdk.kotlinx.coroutines.android)

    testImplementation(localizeSdk.junit)
    testImplementation(localizeSdk.kotlinx.coroutines.test)
}
