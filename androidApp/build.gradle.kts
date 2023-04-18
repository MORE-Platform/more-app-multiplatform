plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    kotlin("android")
    id("io.realm.kotlin") version "1.6.0"
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "io.redlink.more.more_app_mutliplatform.android"
    compileSdk = 33
    defaultConfig {
        applicationId = "io.redlink.more.more_app_mutliplatform.android"
        minSdk = 29
        targetSdk = 33
        versionCode = 2
        versionName = "2.0-rc1"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val composeVersion = "1.3.1"
val workVersion = "2.8.0"
val navVersion = "2.5.3"
val sdk_version = "5.0.2"

dependencies {
    implementation(project(":shared"))
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("io.realm.kotlin:library-base:1.6.0")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.firebase:firebase-analytics-ktx:21.2.1")
    implementation("com.google.firebase:firebase-messaging-ktx:23.1.2")
    implementation("io.github.aakira:napier:2.6.1")
    implementation("com.github.polarofficial:polar-ble-sdk:${sdk_version}")
    implementation("io.reactivex.rxjava3:rxjava:3.1.6")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation(platform("com.google.firebase:firebase-bom:31.2.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-inappmessaging-ktx")
    implementation("com.google.firebase:firebase-inappmessaging-display-ktx")
    implementation("com.google.code.gson:gson:2.10.1")
}
