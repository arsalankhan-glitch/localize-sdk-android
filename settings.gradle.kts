pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("localizeSdk") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}
rootProject.name = "localize-sdk-android"
include(":localize", ":example")
