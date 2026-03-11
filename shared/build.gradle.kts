import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    id("com.android.library")
    id("androidx.room")
    id("com.google.devtools.ksp")
    id("com.rickclephas.kmp.nativecoroutines")
    id("org.openapi.generator").version("7.17.0").apply(true)
    id("dev.icerock.mobile.multiplatform-resources")
}

val generated = "$rootDir/shared/build/generated"
val openApiInputDir = "$rootDir/openapi"
val openApiOutputDir = "$generated/open_api"
val mobileAppApiInput = "$openApiInputDir/MobileAppAPI.yaml"
val mobileAppApiOutputDir = "$openApiOutputDir/mobile_app_api"
val mobileAppApiPackage = "io.redlink.more.services.network.openapi"
val openapiIgnore = "$openApiInputDir/openapi-ignore"

val coroutinesVersion = "1.10.2"
val ktorVersion = "3.4.0"
val napierVersion = "2.7.1"
val serializationVersion = "1.9.0"
val gsonVersion = "2.13.2"
val roomVersion = "2.8.4"
val sqliteVersion = "2.5.2"

val mokoResVersion = "0.25.2"
val mokoGraphicsVersion = "0.10.1"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
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
            export("dev.icerock.moko:resources:$mokoResVersion")
            export("dev.icerock.moko:graphics:$mokoGraphicsVersion")
        }
    }
    sourceSets {
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

            implementation(project.dependencies.platform("org.kotlincrypto.hash:bom:0.7.1"))
            implementation("org.kotlincrypto.hash:md")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
        }

        androidMain.dependencies {
            implementation("androidx.security:security-crypto-ktx:1.1.0")
            implementation("io.ktor:ktor-client-android:$ktorVersion")
            implementation("com.google.code.gson:gson:$gsonVersion")
        }

        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:$ktorVersion")
        }

        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }
        sourceSets["commonMain"].kotlin.srcDirs("$mobileAppApiOutputDir/src/commonMain/kotlin")
    }
}

android {
    namespace = "io.redlink.more"
    compileSdk = 36
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
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

    commonMainApi("dev.icerock.moko:resources:$mokoResVersion")
    commonMainApi("dev.icerock.moko:graphics:$mokoGraphicsVersion")

    commonTestImplementation("dev.icerock.moko:resources-test:$mokoResVersion")
}

multiplatformResources {
    resourcesPackage.set("io.redlink.more")
    resourcesClassName.set("SharedRes")
    iosBaseLocalizationRegion.set("en")
    iosMinimalDeploymentTarget.set("16.2")
}

tasks.register<GenerateTask>(
    "generateOpenApiClasses",
) {
    generatorName.set("kotlin")
    library.set("multiplatform")

    inputSpec.set(mobileAppApiInput)
    outputDir.set(mobileAppApiOutputDir)

    packageName.set(mobileAppApiPackage)
    modelPackage.set("$mobileAppApiPackage.model")
    apiPackage.set("$mobileAppApiPackage.api")

    globalProperties.set(
        mapOf(
            "models" to "",
            "apis" to "",
            "supportingFiles" to "",
            "modelDocs" to "false",
            "apiDocs" to "false"
        )
    )

    configOptions.set(
        mapOf(
            "dateLibrary" to "kotlinx-datetime",
            "enumPropertyNaming" to "UPPERCASE"
        )
    )

    typeMappings.putAll(
        mapOf(
            "object" to "kotlinx.serialization.json.JsonObject"
        )
    )

    importMappings.putAll(
        mapOf(
            "Instant" to "kotlinx.datetime.Instant",
            "kotlinx.serialization.json.JsonObject" to "kotlinx.serialization.json.JsonObject"
        )
    )

    // Let Gradle cache this so it only runs when the YAML changes
    inputs.file(mobileAppApiInput)
    outputs.dir(mobileAppApiOutputDir)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateOpenApiClasses")
}

tasks.withType<com.google.devtools.ksp.gradle.KspAATask>().configureEach {
    dependsOn("generateOpenApiClasses")
}

tasks.withType<Test>().configureEach {
    dependsOn("generateOpenApiClasses")
}