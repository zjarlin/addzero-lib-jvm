@file:OptIn(ExperimentalWasmDsl::class)

package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import site.addzero.gradle.BuildSettings

val libs = the<LibrariesForLibs>()

plugins {
  id("com.android.application")
  id("site.addzero.gradle.plugin.kmp-compose-common-convention")
}

kotlin {
  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
      jvmTarget.set(JvmTarget.fromTarget(libs.versions.jdk.get()))
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(compose.preview)
      implementation(libs.androidx.activity.activity.compose)
    }
  }
}

android {
  namespace = BuildSettings.PACKAGE_NAME
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    applicationId = BuildSettings.Android.ANDROID_APP_ID
    minSdk = libs.versions.android.minSdk.get().toInt()
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

