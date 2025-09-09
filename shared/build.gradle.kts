plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    id("com.android.library")
    id("androidx.room")
    id("com.google.devtools.ksp")
//    id("org.openapi.generator") version "7.15.0"
}

val generated = "$rootDir/shared/build/generated"
val openApiInputDir = "$rootDir/openapi"
val openApiOutputDir = "$generated/open_api"
val mobileAppApiInput = "$openApiInputDir/MobileAppAPI.yaml"
val mobileAppApiOutputDir = "$openApiOutputDir/mobile_app_api"
val mobileAppApiPackage = "io.redlink.more.more_app_multiplatform.services.network.openapi"
val openapiIgnore = "$openApiInputDir/openapi-ignore"

val coroutinesVersion = "1.10.2"
val ktorVersion = "3.2.3"
val napierVersion = "2.7.1"
val serializationVersion = "1.9.0"
val gsonVersion = "2.13.1"
val roomVersion = "2.7.2"
val sqliteVersion = "2.5.2"

//openApiGenerate {
//    generatorName.set("kotlin")
//    inputSpec.set(mobileAppApiInput)
//    outputDir.set(mobileAppApiOutputDir)
//    packageName.set(mobileAppApiPackage)
//    ignoreFileOverride.set(openapiIgnore)
//
//    configOptions.set(
//        mapOf(
//            "library" to "multiplatform",
//            "serializationLibrary" to "kotlinx_serialization",
//            "dateLibrary" to "kotlinx-datetime",
//            "enumPropertyNaming" to "UPPERCASE",
//            "omitGradleWrapper" to "true",
//            "omitGradlePlugin" to "true",
//            "sourceFolder" to "", // This might help with package structure
//            "modelPackage" to "${mobileAppApiPackage}.models",
//            "apiPackage" to "${mobileAppApiPackage}.apis"
//        )
//    )
//
//    typeMappings.set(
//        mapOf(
//            "object" to "JsonObject"
//        )
//    )
//
//    importMappings.set(
//        mapOf(
//            "JsonObject" to "kotlinx.serialization.json.JsonObject"
//        )
//    )
//}

//tasks.register("fixDuplicateSerializable") {
//    dependsOn("openApiGenerate")
//    doLast {
//        fileTree("$mobileAppApiOutputDir/src/main/kotlin").matching {
//            include("**/*.kt")
//        }.forEach { file ->
//            var content = file.readText()
//            // Fix double @Serializable annotations
//            content = content
//                .replace("@Serializable@Serializable", "@Serializable")
////                .replace("sealed sealed", "sealed")
//                .replace(
//                    "kotlin.collections.Map<kotlin.String, kotlin.Any>,",
//                    "kotlinx.serialization.json.JsonObject,"
//                )
//
//            // Fix ObservationData.kt serialization issue
////            if (file.name == "ObservationData.kt") {
////                content = content.replace(
////                    "@SerialName(value = \"dataValue\") @Required val dataValue: kotlin.collections.Map<kotlin.String, kotlin.Any>,",
////                    "@SerialName(value = \"dataValue\") @Required val dataValue: kotlinx.serialization.json.JsonObject,"
////                )
////            }
//
//            // Fix PushNotificationConfig.kt interface issue
//            if (file.name == "PushNotificationConfig.kt") {
//                content = content.replace(
//                    "interface PushNotificationConfig {",
//                    "sealed interface PushNotificationConfig {"
//                )
//            }
//
//            file.writeText(content)
//        }
//    }
//}
//
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    dependsOn("fixDuplicateSerializable")
//}
//
//// Ensure KSP tasks depend on OpenAPI generation
//tasks.withType<com.google.devtools.ksp.gradle.KspTask> {
//    dependsOn("fixDuplicateSerializable")
//}

// Add this after your existing configurations

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
//        commonMain {
//            kotlin.srcDir("$mobileAppApiOutputDir/src/main/kotlin")
//        }
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.github.aakira:napier:$napierVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            implementation("io.ktor:ktor-client-auth:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")
            implementation("dev.tmapps:konnection:1.4.5")

            // Room common dependencies
            implementation("androidx.room:room-runtime:$roomVersion")
            implementation("androidx.sqlite:sqlite-bundled:$sqliteVersion")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation("androidx.security:security-crypto-ktx:1.1.0")
            implementation("io.ktor:ktor-client-android:$ktorVersion")
            implementation("com.google.code.gson:gson:$gsonVersion")
        }

        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:$ktorVersion")
        }
    }
}

android {
    namespace = "io.redlink.more.more_app_multiplatform"
    compileSdk = 36
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", "androidx.room:room-compiler:$roomVersion")
    add("kspIosArm64", "androidx.room:room-compiler:$roomVersion")
    add("kspIosSimulatorArm64", "androidx.room:room-compiler:$roomVersion")
    add("kspIosX64", "androidx.room:room-compiler:$roomVersion")
}

tasks.register("testClasses") {
    doLast {
        println("This is a dummy testClasses task")
    }
}

// Add this after your existing KSP configuration
//afterEvaluate {
//    tasks.matching { task ->
//        task.name.startsWith("ksp") && task.name.contains("Kotlin")
//    }.configureEach {
//        dependsOn("fixDuplicateSerializable")
//    }
//}