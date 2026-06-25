import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.kunnn.totap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kunnn.totap"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        // Appwrite config injected from local.properties / CI env (never hardcoded in source)
        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localPropsFile.inputStream().use { localProps.load(it) }
        }

        buildConfigField("String", "APPWRITE_PROJECT_ID",
            "\"${localProps.getProperty("APPWRITE_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "APPWRITE_PROJECT_NAME",
            "\"${localProps.getProperty("APPWRITE_PROJECT_NAME") ?: ""}\"")
        buildConfigField("String", "APPWRITE_PUBLIC_ENDPOINT",
            "\"${localProps.getProperty("APPWRITE_PUBLIC_ENDPOINT") ?: ""}\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // R8 obfuscation is enabled (per spec §8.7). The complete keep-rule set
            // and AGP's default ProGuard file are wired in during Phase 8 when
            // release builds first ship; for Phase 1 only the debug build is needed.
            proguardFiles("proguard-rules.pro")
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

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":feature:home"))

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
