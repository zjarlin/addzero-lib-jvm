pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    google()
  }
  plugins {
    id("site.addzero.kcp.multireceiver") version "2026.03.13"
    id("site.addzero.kcp.transform-overload") version "2026.03.13"
  }
}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "2026.01.11"
}
includeBuild("build-logic")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("./build-logic/gradle/libs.versions.toml"))
    }
  }
}


// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-03-17T09:59:50.649192
// Loaded: 4, Excluded: 0, Total: 4
include(":lib:biz:spec-iot")
include(":lib:ksp:metadata:spring2ktor-server-core")
include(":lib:ksp:metadata:spring2ktor-server-processor")
include(":lib:ksp:metadata:spring2ktor-server-smoke")
// <<< Gradle Module Sleep: End Of Block <<<
