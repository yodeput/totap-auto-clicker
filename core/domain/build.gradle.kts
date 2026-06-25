plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.kunnn.totap.core.domain"
    compileSdk = 35

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Domain is pure Kotlin: NO Android dependencies allowed in model/planner code.
    // Unit tests run on the JVM (fast, no device).
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
