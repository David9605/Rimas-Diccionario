import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Credenciales de firma: keystore.properties (local) o variables de entorno (GitHub Actions).
val ksProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun ksProp(name: String, env: String): String? = ksProps.getProperty(name) ?: System.getenv(env)
val ksFile: String? = ksProp("storeFile", "KEYSTORE_FILE")
val hasKeystore: Boolean = ksFile != null

android {
    namespace = "com.davidgonzalez.rimasdiccionario"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.davidgonzalez.rimasdiccionario"
        minSdk = 26
        targetSdk = 36          // Google Play exige API 36 desde el 31-ago-2026
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = rootProject.file(ksFile!!)
                storePassword = ksProp("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = ksProp("keyAlias", "KEY_ALIAS")
                keyPassword = ksProp("keyPassword", "KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasKeystore) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    lint { abortOnError = false }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    testImplementation("junit:junit:4.13.2")
}
