pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io" )}
    }
}

rootProject.name = "More_app_mutliplatform"
include(":androidApp")
include(":shared")