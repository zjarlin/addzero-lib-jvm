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
// Generated at: 2026-03-20T09:16:20.913039
// Loaded: 1, Excluded: 0, Total: 1
include(":lib:ksp:metadata:ioc:ioc-core")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:kcp:kcp-i18n")
include(":example:example-kcp-i18n")
