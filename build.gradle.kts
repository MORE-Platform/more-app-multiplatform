buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.6")
    }
    repositories {
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository

        maven {
            url = uri("https://maven.pkg.github.com/redlink-gmbh/ignored")
            credentials {
                username = findProperty("io.redlink-gmbh.mvn.user") as String?
                password = findProperty("io.redlink-gmbh.mvn.key") as String?
            }
        }
    }
}

plugins {
    id("com.android.application").version("8.13.2").apply(false)
    id("com.android.library").version("8.13.2").apply(false)
    kotlin("android").version("2.3.10").apply(false)
    kotlin("multiplatform").version("2.3.10").apply(false)
    kotlin("plugin.serialization").version("2.3.10").apply(false)
    id("org.jetbrains.kotlin.plugin.compose").version("2.3.10").apply(false)
    id("androidx.room").version("2.8.4").apply(false)
    id("com.google.devtools.ksp").version("2.3.5").apply(false)

    id("com.rickclephas.kmp.nativecoroutines").version("1.0.1").apply(false)
    id("dev.icerock.mobile.multiplatform-resources").version("0.25.2").apply(false)
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
