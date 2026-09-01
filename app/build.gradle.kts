import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}


// ==========================================================
// Load secrets from local.properties so they're available as
// project properties for buildConfigField below. local.properties
// is git-ignored, unlike gradle.properties.
//
// project.findProperty() alone would NOT see them — Gradle
// only auto-loads gradle.properties into project properties.
// ==========================================================


val localProperties: Properties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        f.inputStream().use { stream -> load(stream) }
    }
}

fun localProp(key: String): String =
    localProperties.getProperty(key)
        ?: project.findProperty(key)?.toString()
        ?: ""


android {
    namespace = "com.example.nekoeco"

    compileSdk {
        version = release(37) {
        }
    }

    defaultConfig {
        applicationId = "com.example.nekoeco"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

buildFeatures {
    compose = true
    buildConfig = true
}


// ==========================================
// Kotlin: skip metadata-version check so the
// Supabase SDK (built with Kotlin 2.1.20) can be
// consumed from a project on Kotlin 2.2.10.
// ==========================================

}

dependencies {

    // ==========================================
    // Compose
    // ==========================================

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation.layout)

    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.compose.ui)

    implementation(libs.androidx.compose.ui.graphics)

    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation("androidx.compose.material:material-icons-extended")


    // ==========================================
    // AndroidX
    // ==========================================

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.espresso.core)

    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2"
    )
    implementation(libs.androidx.material3)

    implementation(libs.androidx.navigation.compose)


    // ==========================================
    // CameraX
    // ==========================================

    implementation("androidx.camera:camera-core:1.4.2")

    implementation("androidx.camera:camera-camera2:1.4.2")

    implementation("androidx.camera:camera-lifecycle:1.4.2")

    implementation("androidx.camera:camera-view:1.4.2")


    // ==========================================
    // Retrofit
    // ==========================================

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    implementation("com.squareup.retrofit2:converter-gson:2.11.0")


    // ==========================================
    // Gson
    // ==========================================

    implementation("com.google.code.gson:gson:2.10.1")


    // ==========================================
    // Coil
    // ==========================================

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation(libs.androidx.ui)


    // ==========================================
    // Supabase (Postgrest only — no Auth module)
    // ==========================================

    implementation(platform("io.github.jan-tennert.supabase:bom:${libs.versions.supabase.get()}"))
    implementation(libs.supabase.core)
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.android)


    // ==========================================
    // Testing
    // ==========================================

    testImplementation(libs.junit)

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}