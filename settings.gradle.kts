//pluginManagement {
//  repositories {
//    mavenLocal()
//    gradlePluginPortal()
//    mavenCentral()
//    google()
//  }
//  plugins {
//    id("site.addzero.kcp.i18n") version "2026.03.13"
//    id("site.addzero.kcp.multireceiver") version "2026.03.13"
//    id("site.addzero.kcp.transform-overload") version "2026.03.13"
//  }
//}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
includeBuild("checkouts/build-logic")

// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-03-26T15:53:43.491892
// Loaded: 3, Excluded: 0, Total: 3
include(":lib:compose:compose-model-component")
include(":lib:gradle-plugin:project-plugin:gradle-processor-buddy")
include(":lib:ksp:route:route-processor")
// <<< Gradle Module Sleep: End Of Block <<<
