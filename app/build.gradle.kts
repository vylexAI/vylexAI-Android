import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

kapt {
    correctErrorTypes = true
}

/**
 * Release signing config.
 *
 * Resolution order:
 *   1. CI: environment variables VYLEX_KEYSTORE_PATH / _PASSWORD / _KEY_ALIAS / _KEY_PASSWORD
 *   2. Local: `keystore.properties` at the repo root (gitignored)
 *   3. Nothing configured → release builds fall back to the debug signing key
 *      with a visible warning. Lets CI + local dev still produce a release
 *      AAB/APK for smoke tests; production uploads require a real key.
 */
val releaseSigning: (
    com.android.build.api.dsl.SigningConfig
) -> Boolean = fn@{ config ->
    val envPath = System.getenv("VYLEX_KEYSTORE_PATH")
    if (!envPath.isNullOrBlank()) {
        config.storeFile = file(envPath)
        config.storePassword = System.getenv("VYLEX_KEYSTORE_PASSWORD")
        config.keyAlias = System.getenv("VYLEX_KEYSTORE_KEY_ALIAS")
        config.keyPassword = System.getenv("VYLEX_KEYSTORE_KEY_PASSWORD")
        return@fn true
    }
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) {
        val props = Properties().apply { propsFile.inputStream().use(::load) }
        config.storeFile = file(props.getProperty("storeFile"))
        config.storePassword = props.getProperty("storePassword")
        config.keyAlias = props.getProperty("keyAlias")
        config.keyPassword = props.getProperty("keyPassword")
        return@fn true
    }
    false
}

android {
    namespace = "com.vylexai.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vylexai.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            val configured = releaseSigning(this)
            if (!configured) {
                logger.warn(
                    "VylexAI: release signing not configured — using debug key. " +
                        "Set VYLEX_KEYSTORE_* env vars or add keystore.properties (see KEYSTORE.md)."
                )
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            // 10.0.2.2 = the host machine from the Android emulator's POV.
            buildConfigField("String", "COORDINATOR_BASE_URL", "\"http://10.0.2.2:8000/\"")
            // Play Integrity cloud project number (Google Cloud). 0 = unconfigured →
            // PlayIntegrityTokenProvider short-circuits to null. Override via CI.
            buildConfigField(
                "long",
                "PLAY_INTEGRITY_CLOUD_PROJECT",
                (System.getenv("VYLEX_PLAY_INTEGRITY_PROJECT") ?: "0") + "L"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Replaced once Stage 2.9 deploys the coordinator behind a TLS domain.
            buildConfigField("String", "COORDINATOR_BASE_URL", "\"https://api.vylexai.com/\"")
            buildConfigField(
                "long",
                "PLAY_INTEGRITY_CLOUD_PROJECT",
                (System.getenv("VYLEX_PLAY_INTEGRITY_PROJECT") ?: "0") + "L"
            )
            // Attach the release signing if it's configured; fall back to debug otherwise
            // so `./gradlew bundleRelease` always succeeds in CI without secrets.
            signingConfig = signingConfigs.findByName("release")
                ?.takeIf { it.storeFile != null }
                ?: signingConfigs.getByName("debug")
        }
    }

    bundle {
        language { enableSplit = true }
        density { enableSplit = true }
        abi { enableSplit = true }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)
    implementation(libs.play.integrity)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Hilt (kapt — more stable than KSP with Hilt on Kotlin 2.1 right now)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.work)
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Network
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // ML runtimes (present but not invoked in Stage 1; keeps architecture primed)
    implementation(libs.onnxruntime.android)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)

    // Test — network
    testImplementation(libs.okhttp.mockwebserver)
}
