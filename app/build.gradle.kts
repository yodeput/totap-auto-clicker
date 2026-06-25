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
        val props = rootProject.file("local.properties")
            .takeIf { it.exists() }
            ?.let { java.util.Properties().apply { it.inputStream().use { s -> load(s) } } }

        buildConfigField("String", "APPWRITE_PROJECT_ID",
            "\"${props?.getProperty("APPWRITE_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "APPWRITE_PROJECT_NAME",
            "\"${props?.getProperty("APPWRITE_PROJECT_NAME") ?: ""}\"")
        buildConfigField("String", "APPWRITE_PUBLIC_ENDPOINT",
            "\"${props?.getProperty("APPWRITE_PUBLIC_ENDPOINT") ?: ""}\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProGuardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
