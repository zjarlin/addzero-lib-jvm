//pluginManagement {
//  repositories {
//    mavenLocal()
//    gradlePluginPortal()
//    mavenCentral()
//    google()
//  }
//  plugins {
//    id("site.addzero.kcp.all-object-jvm-static") version "+"
//    id("site.addzero.kcp.i18n") version "+"
//    id("site.addzero.kcp.multireceiver") version "+"
//    id("site.addzero.kcp.transform-overload") version "+"
//    id("site.addzero.kcp.spread-pack") version "+"
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
// Generated at: 2026-04-07T08:39:44.598541
// Loaded: 5, Excluded: 0, Total: 5
include(":example:example-spread-pack")
include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base")
include(":lib:gradle-plugin:project-plugin:gradle-publish-budy")
include(":lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin")
include(":lib:tool-kmp:network-starter")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin")
include(":lib:kcp:spread-pack:kcp-spread-pack-plugin")
