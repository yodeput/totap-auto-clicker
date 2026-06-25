import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

/**
 * Resolve version name/code from the pushed git tag when present (e.g. "v1.2.3").
 * Tags are the source of truth for releases (spec §8.5). Falls back to a dev
 * default for non-tag builds (debug, PR CI runs).
 *
 *   v1.2.3   -> versionName=1.2.3, versionCode=10203
 *   v1.2.3b4 -> versionName=1.2.3-b4, versionCode=1020304
 */
fun deriveVersion(): Pair<String, Int> {
    val tag: String? = try {
        providers.exec {
            commandLine("git", "describe", "--tags", "--exact-match")
        }.standardOutput.asText.get().trim().takeIf { it.isNotEmpty() }
    } catch (_: Exception) { null }

    val fallbackName = "0.1.0-dev"
    if (tag == null) return fallbackName to 1

    val core = tag.removePrefix("v")
    // split optional suffix after the core semver (e.g. "1.2.3-rc1")
    val match = Regex("""^(\d+)\.(\d+)\.(\d+)(?:-([0-9A-Za-z.-]+))?$""").find(core)
        ?: return fallbackName to 1
    val (maj, min, patch, suffix) = match.destructured
    val name = if (suffix.isEmpty()) "$maj.$min.$patch" else "$maj.$min.$patch-$suffix"
    // versionCode must be an int and always increase; pack as MMmmpp + 2-digit suffix index.
    val numericSuffix = if (suffix.isEmpty()) 0 else (suffix.filter { it.isDigit() }.take(2).ifEmpty { "0" }).toInt()
    val code = (maj.toInt() * 10000) + (min.toInt() * 100) + patch.toInt() * 1 + numericSuffix
    return name to code
}

android {
    namespace = "com.kunnn.totap"
    compileSdk = 35

    val (versionName, versionCode) = deriveVersion()

    defaultConfig {
        applicationId = "com.kunnn.totap"
        minSdk = 29
        targetSdk = 35
        this.versionCode = versionCode
        this.versionName = versionName

        // Appwrite config injected from local.properties / CI env (never hardcoded in source)
        val localProps = readLocalProps()
        buildConfigField("String", "APPWRITE_PROJECT_ID",
            "\"${localProps.getProperty("APPWRITE_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "APPWRITE_PROJECT_NAME",
            "\"${localProps.getProperty("APPWRITE_PROJECT_NAME") ?: ""}\"")
        buildConfigField("String", "APPWRITE_PUBLIC_ENDPOINT",
            "\"${localProps.getProperty("APPWRITE_PUBLIC_ENDPOINT") ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            val localProps = readLocalProps()
            // CI injects these as env vars; local dev reads them from local.properties.
            // storeFile resolves relative to the module dir, so use rootProject.file for the
            // path convention "keystore/totap-release.jks" at the project root.
            val keystorePath = System.getenv("SIGNING_KEYSTORE_PATH")
                ?: localProps.getProperty("SIGNING_KEYSTORE_PATH")
                ?: "keystore/totap-release.jks"
            storeFile = rootProject.file(keystorePath)
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                ?: localProps.getProperty("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                ?: localProps.getProperty("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
                ?: localProps.getProperty("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            // R8 obfuscation (spec §8.7). Complete keep-rule set wired in Phase 8;
            // essential keeps below are enough for the Phase-1 surface.
            proguardFiles("proguard-rules.pro")
        }
    }

    // Per-ABI APKs + universal (spec §8). Cuts download size ~60-70% for sideloaders.
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86_64")
            isUniversalApk = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// CI sets version codes per ABI via this so universal > all per-ABI (so Play/A-B can rank them).
// (Per-output versionCode override intentionally omitted: this app ships via GitHub Releases
// for sideloading, where Play's per-ABI ranking rules don't apply. The single versionCode
// from deriveVersion() is sufficient and keeps the build script simple.)

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:autoclick"))
    implementation(project(":feature:home"))
    implementation(project(":feature:onboarding"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
}

// Helpers ---------------------------------------------------------------------

fun readLocalProps(): Properties {
    val props = Properties()
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { props.load(it) }
    return props
}
