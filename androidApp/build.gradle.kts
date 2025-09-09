import java.util.Base64
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    kotlin("android")
    id("io.realm.kotlin") version "1.14.1"
    id("com.google.firebase.crashlytics")
}

fun loadEnvFromFile(): Properties {
    val envProps = Properties()
    val envFiles = listOf(
        File(rootProject.rootDir, ".env"),
        File(rootProject.rootDir, "local.properties"),
        File(project.projectDir, ".env"),
        File(project.projectDir, "signing.properties")
    )

    envFiles.forEach { envFile ->
        if (envFile.exists()) {
            println("Loading environment variables from: ${envFile.absolutePath}")
            try {
                envFile.inputStream().use { input ->
                    envProps.load(input)
                }
            } catch (e: Exception) {
                println("Failed to load ${envFile.name}: ${e.message}")
            }
        }
    }

    return envProps
}

fun getEnvOrProperty(key: String, envProps: Properties): String? {
    return System.getenv(key) ?: envProps.getProperty(key)
}

val envProps = loadEnvFromFile()

android {
    namespace = "io.redlink.more.app.android"
    compileSdk = 36
    defaultConfig {
        applicationId = "ac.at.lbg.dhp.more"
        minSdk = 29
        targetSdk = 36
        versionCode = 28
        versionName = "4.0.28"
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

    signingConfigs {
        create("release") {
            val keystorePath = getEnvOrProperty("ANDROID_KEYSTORE_PATH", envProps) ?: ""
            val keystoreBase64 = getEnvOrProperty("ANDROID_KEYSTORE_BASE64", envProps) ?: ""
            this.storePassword = getEnvOrProperty("ANDROID_KEYSTORE_PASSWORD", envProps) ?: ""
            this.keyAlias = getEnvOrProperty("ANDROID_KEY_ALIAS", envProps) ?: ""
            this.keyPassword = getEnvOrProperty("ANDROID_KEY_PASSWORD", envProps) ?: ""

            println("Keystore path: ${keystorePath.length}\n keystoreBase64: ${keystoreBase64.length}\n keystorePassword: ${this.storePassword?.length}\n keyAlias: ${this.keyAlias?.length}\n keyPassword: ${this.keyPassword?.length}")

            val storeFile: File? = if (keystorePath.isNotEmpty()) {
                val keystoreFile = File(keystorePath)
                if (keystoreFile.exists()) {
                    println("Using keystore path from configuration: $keystorePath")
                    keystoreFile
                } else {
                    println("Keystore file does not exist at: $keystorePath")
                    null
                }
            } else if (keystoreBase64.isNotEmpty()) {
                try {
                    println("Using keystore from base64 configuration")
                    val decodedBytes = Base64.getDecoder().decode(keystoreBase64)
                    val file = File.createTempFile("keystore", ".jks")
                    file.deleteOnExit()
                    file.writeBytes(decodedBytes)

                    file
                } catch (e: Exception) {
                    println("Failed to decode base64 keystore: ${e.message}")
                    null
                }
            } else {
                null
            }
            storeFile?.let {
                this.storeFile = it
            } ?: run {
                println("Keystore file not found, falling back to debug keystore")
                this.storeFile = File(System.getProperty("user.home"), ".android/debug.keystore")
                this.storePassword = "android"
                this.keyAlias = "androiddebugkey"
                this.keyPassword = "android"
            }
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

            val releaseSigningConfig = signingConfigs.getByName("release")
            if (releaseSigningConfig.storeFile != null) {
                signingConfig = releaseSigningConfig
            } else {
                println("Warning: No signing configuration available. Using debug signing.")
            }

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

// ... rest of your dependencies ...

val composeVersion = "1.6.8"
val workVersion = "2.9.0"
val navVersion = "2.7.7"
val polarSDKVersion = "5.6.0"
val ktorVersion = "2.3.12"

dependencies {
    implementation(project(":shared"))
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.fragment:fragment:1.8.2")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("io.realm.kotlin:library-base:1.13.0")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.firebase:firebase-analytics-ktx:22.0.2")
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")
    implementation("io.github.aakira:napier:2.7.1")
    implementation("com.github.polarofficial:polar-ble-sdk:${polarSDKVersion}")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-inappmessaging-ktx")
    implementation("com.google.firebase:firebase-inappmessaging-display-ktx")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.acsbendi:Android-Request-Inspector-WebView:1.0.3")
    implementation("androidx.lifecycle:lifecycle-process:2.8.4")
    //Google ML Kit for QR Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
}
