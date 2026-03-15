package site.addzero.buildlogic.kmp

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("com.android.lint")
    id("site.addzero.buildlogic.kmp.kmp-convention")
}

val libs = versionCatalogs.named("libs")

val androidNamespace = libs.findVersion("android-namespace").get().requiredVersion
val compileSdkVersion = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
val minSdkVersion = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
val testRunner = libs.findVersion("android-testInstrumentationRunner").get().requiredVersion
val jdk11Version = libs.findVersion("jdk11").get().requiredVersion.toInt()

kotlin {
    androidLibrary {
        namespace = androidNamespace
        compileSdk = compileSdkVersion
        minSdk = minSdkVersion

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = testRunner
        }
    }
}

kotlin {
    jvmToolchain(jdk11Version)
}

