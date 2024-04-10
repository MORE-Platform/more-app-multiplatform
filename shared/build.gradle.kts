plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    id("com.android.library")
    id("io.realm.kotlin") version "1.13.0"
}

val generated = "$rootDir/shared/build/generated"
val openApiInputDir = "$rootDir/openapi"
val openApiOutputDir = "$generated/open_api"
val mobileAppApiInput = "$openApiInputDir/MobileAppAPI.yaml"
val mobileAppApiOutputDir = "$openApiOutputDir/mobile_app_api"
val mobileAppApiPackage = "io.redlink.more.more_app_multiplatform.openapi"
val openapiIgnore = "$openApiInputDir/openapi-ignore"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
        publishLibraryVariants("release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            freeCompilerArgs = freeCompilerArgs + "-Xallocator=std"
        }
    }

    val coroutinesVersion = "1.7.3"
    val ktorVersion = "2.3.7"
    val napierVersion = "2.7.1"
    val serializationVersion = "1.6.0"
    val gsonVersion = "2.10.1"

    sourceSets {
        val commonMain by getting {
            dependencies {
                kotlin.srcDir(mobileAppApiOutputDir)

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")


                implementation("io.realm.kotlin:library-base:1.13.0")

                implementation("io.github.aakira:napier:$napierVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("io.ktor:ktor-client-auth:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")

            }

        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")

                implementation("io.ktor:ktor-client-android:$ktorVersion")
                implementation("com.google.code.gson:gson:$gsonVersion")
            }
        }
        val androidUnitTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "io.redlink.more.more_app_multiplatform"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

task("testClasses").doLast {
    println("This is a dummy testClasses task")
}