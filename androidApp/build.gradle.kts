plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    kotlin("android")
    id("io.realm.kotlin") version "1.14.1"
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "io.redlink.more.app.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "ac.at.lbg.dhp.more"
        minSdk = 29
        targetSdk = 34
        versionCode = 8
        versionName = "4.0.9"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            buildConfigField("long", "VERSION_CODE", "${defaultConfig.versionCode}")
            buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
        }
        release {
            buildConfigField("long", "VERSION_CODE", "${defaultConfig.versionCode}")
            buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

val composeVersion = "1.6.7"
val workVersion = "2.9.0"
val navVersion = "2.7.7"
val polarSDKVersion = "5.5.0"

dependencies {
    implementation(project(":shared"))
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.fragment:fragment:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("io.realm.kotlin:library-base:1.13.0")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.firebase:firebase-analytics-ktx:22.0.0")
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")
    implementation("io.github.aakira:napier:2.7.1")
    implementation("com.github.polarofficial:polar-ble-sdk:${polarSDKVersion}")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-inappmessaging-ktx")
    implementation("com.google.firebase:firebase-inappmessaging-display-ktx")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.acsbendi:Android-Request-Inspector-WebView:1.0.3")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
}
