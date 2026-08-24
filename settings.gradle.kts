pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
// Note: settings.gradle.kts plugin blocks resolve before the version catalog is available,
// so this one keeps a hardcoded version rather than using libs.plugins.foojay.resolver.convention.

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
}

rootProject.name = "More_app_mutliplatform"
include(":androidApp")
include(":shared")