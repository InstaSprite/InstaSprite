import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobuf)
    id("kotlin-parcelize")
}

val firebaseConfigured = file("google-services.json").isFile ||
        fileTree("src") { include("**/google-services.json") }.files.isNotEmpty()
val releaseSigningConfigured = listOf("KEYSTORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")
    .all { !System.getenv(it).isNullOrBlank() }
if (firebaseConfigured) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.instasprite.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.instasprite.app"
        buildConfigField("boolean", "CRASH_REPORTING_CONFIGURED", firebaseConfigured.toString())
        minSdk = 26
        targetSdk = 36
        val ciVersionCode = project.findProperty("ciVersionCode")?.toString()?.toIntOrNull()
        versionCode = ciVersionCode ?: 3
        versionName = "0.8.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    flavorDimensions += "env"

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "InstaSprite Dev")
        }
        create("prod") {
            dimension = "env"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (releaseSigningConfigured) signingConfigs.getByName("release") else null
        }
        debug {
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    bundle {
        language {
            enableSplit = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    // --- Core Android & Kotlin ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.palette.ktx)



    // --- Jetpack Compose UI ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.navigation3.viewmodel)
    implementation(libs.reorderable)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    // --- Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // --- UI Utilities & Theme ---
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.core.splashscreen)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.datastore) // Proto DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.zoomable)
    implementation(libs.compose.material.icons)
    implementation(libs.easycrop)

    // --- Persistence / Storage ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
    implementation(libs.annotations) // Javax Annotations (used by Room, etc.)
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.androidx.work.runtime.ktx)

    // --- Network ---
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.coil.network.okhttp)
    implementation(libs.okhttp3.logging.interceptor)
    
    // --- Testing ---
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.paging.testing)

    // Android Instrumentation Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // --- Debugging ---
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.play.services.auth.coroutines)

}
