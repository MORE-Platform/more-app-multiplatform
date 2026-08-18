plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kmp.nativecoroutines) apply false
    alias(libs.plugins.moko.resources) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
