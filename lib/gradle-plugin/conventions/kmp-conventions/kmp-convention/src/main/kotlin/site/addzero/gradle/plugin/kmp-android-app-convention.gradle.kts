@file:OptIn(ExperimentalWasmDsl::class)

package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import site.addzero.gradle.BuildSettings

val libs = the<VersionCatalogsExtension>().named("libs")

plugins {
  id("com.android.application")
  id("site.addzero.gradle.plugin.kmp-compose-common-convention")
}

kotlin {
  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
      jvmTarget.set(JvmTarget.fromTarget(libs.ver("jdk")))
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(compose.preview)
      implementation(libs.lib("androidx-activity-activity-compose"))
    }
  }
}

android {
  namespace = BuildSettings.PACKAGE_NAME
  compileSdk = libs.ver("android-compileSdk").toInt()
  defaultConfig {
    applicationId = BuildSettings.Android.ANDROID_APP_ID
    minSdk = libs.ver("android-minSdk").toInt()
    targetSdk = libs.ver("android-targetSdk").toInt()
    val vname = libs.ver("android-biz-version")
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
    val toVersion = JavaVersion.toVersion(libs.ver("jdk"))
    sourceCompatibility = toVersion
    targetCompatibility = toVersion
  }
}

