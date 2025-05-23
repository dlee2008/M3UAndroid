pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
    }
}
rootProject.name = "M3U"
include(":app:smartphone", ":app:tv", ":app:extension")
include(":core", ":core:foundation", ":core:extension")
include(":data")
include(
    ":business:foryou",
    ":business:favorite",
    ":business:setting",
    ":business:playlist",
    ":business:playlist-configuration",
    ":business:channel",
    ":business:extension",
)
include(":baselineprofile:smartphone", ":baselineprofile:tv")
include(":i18n")
include(
    ":lint:annotation",
    ":lint:processor"
)
