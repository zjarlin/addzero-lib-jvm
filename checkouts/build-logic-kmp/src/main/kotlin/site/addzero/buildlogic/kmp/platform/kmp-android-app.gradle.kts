@file:OptIn(ExperimentalWasmDsl::class)

package site.addzero.buildlogic.kmp.platform

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import site.addzero.gradle.BuildSettings

val libs = the<LibrariesForLibs>()

plugins {
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.multiplatform")
}
kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jdk.get()))
        }
    }


    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
    }
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

android {
    namespace = BuildSettings.PACKAGE_NAME
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = BuildSettings.Android.ANDROID_APP_ID
        minSdk = libs.versions.android.minSdk.get().toInt()
        //noinspection OldTargetApi
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        val vname = libs.versions.android.biz.version.get()
        versionName = vname
        versionCode = vname.toDouble().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts += "META-INF/INDEX.LIST"
        }
    }
    buildTypes {
        getByName(BuildSettings.Android.BUILD_TYPE) {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        val toVersion = JavaVersion.toVersion(libs.versions.jdk.get())
        sourceCompatibility = toVersion
        targetCompatibility = toVersion
    }
}
dependencies {
    debugImplementation(compose.uiTooling)
}
