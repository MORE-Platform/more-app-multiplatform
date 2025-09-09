buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.2")
    }
    repositories {
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository
    }
}

plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("8.12.0").apply(false)
    id("com.android.library").version("8.12.0").apply(false)
    kotlin("android").version("2.1.0").apply(false)
    kotlin("multiplatform").version("2.1.0").apply(false)
    kotlin("plugin.serialization").version("2.1.0").apply(false)
    id("org.jetbrains.kotlin.plugin.compose").version("2.1.0").apply(false)
    id("androidx.room").version("2.7.2").apply(false)
    id("com.google.devtools.ksp").version("2.1.0-1.0.29").apply(false)
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
