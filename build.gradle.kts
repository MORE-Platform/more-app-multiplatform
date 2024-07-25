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
    id("com.android.application").version("8.2.2").apply(false)
    id("com.android.library").version("8.2.2").apply(false)
    kotlin("android").version("1.9.23").apply(false)
    kotlin("multiplatform").version("1.9.23").apply(false)
    kotlin("plugin.serialization").version("1.9.23").apply(false)
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}