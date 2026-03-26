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


// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-03-26T09:52:15.389097
// Loaded: 5, Excluded: 0, Total: 5
include(":lib:gradle-plugin:settings-plugin:gradle-git-dependency")
include(":lib:ksp:logger-api")
include(":lib:ksp:logger-implementation")
include(":lib:ksp:metadata:ioc:ioc-core")
include(":lib:ksp:route:route-core")
// <<< Gradle Module Sleep: End Of Block <<<
