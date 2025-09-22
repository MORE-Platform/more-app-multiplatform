buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.3")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.6")
    }
    repositories {
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository
    }
}

plugins {
    id("com.android.application").version("8.13.0").apply(false)
    id("com.android.library").version("8.13.0").apply(false)
    kotlin("android").version("2.2.10").apply(false)
    kotlin("multiplatform").version("2.2.10").apply(false)
    kotlin("plugin.serialization").version("2.2.10").apply(false)
    id("org.jetbrains.kotlin.plugin.compose").version("2.2.10").apply(false)
    id("androidx.room").version("2.7.2").apply(false)
    id("com.google.devtools.ksp").version("2.2.10-2.0.2").apply(false)

    id("com.rickclephas.kmp.nativecoroutines").version("1.0.0-ALPHA-47").apply(false)
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
