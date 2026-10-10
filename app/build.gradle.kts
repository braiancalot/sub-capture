plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val commitCount: Int = providers.exec { commandLine("git", "rev-list", "--count", "HEAD") }
    .standardOutput.asText.get().trim().toInt()
val releaseKeystorePassword: String? = providers.environmentVariable("RELEASE_KEYSTORE_PASSWORD").orNull

android {
    namespace = "br.com.teshi.subcapture"
    compileSdk = 37

    defaultConfig {
        applicationId = "br.com.teshi.subcapture"
        minSdk = 34
        targetSdk = 36
        versionCode = commitCount
        versionName = providers.gradleProperty("releaseVersionName").getOrElse("dev")
    }

    signingConfigs {
        create("release") {
            storeFile = providers.environmentVariable("RELEASE_KEYSTORE_PATH").orNull?.let(::file)
            storePassword = releaseKeystorePassword
            keyAlias = "subcapture"
            keyPassword = releaseKeystorePassword
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.mlkit.text.recognition)

    testImplementation(libs.junit)
}
