package site.addzero.buildlogic.kmp

plugins {
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = versionCatalogs.named("libs")

val androidNamespace = libs.findVersion("android-namespace").get().requiredVersion
val applicationIdValue = libs.findVersion("android-applicationId").get().requiredVersion
val testRunner = libs.findVersion("android-testInstrumentationRunner").get().requiredVersion

android {
    namespace = androidNamespace
    compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
    defaultConfig {
        applicationId = applicationIdValue
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
        targetSdk = libs.findVersion("android-targetSdk").get().requiredVersion.toInt()
        versionCode = libs.findVersion("android-app-versionCode").get().requiredVersion.toInt()
        versionName = libs.findVersion("android-app-versionName").get().requiredVersion
        testInstrumentationRunner = testRunner
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        val jdkVersion = libs.findVersion("jdk11").get().requiredVersion.toInt()
        sourceCompatibility = JavaVersion.toVersion(jdkVersion)
        targetCompatibility = JavaVersion.toVersion(jdkVersion)
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Android dependencies can be added here
}
