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
// Generated at: 2026-04-06T20:47:13.865233200
// Loaded: 17, Excluded: 0, Total: 17
include(":example:example-spread-pack")
include(":lib:api:api-netease")
include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base")
include(":lib:ksp:metadata:modbus:modbus-ksp-c-contract")
include(":lib:ksp:metadata:modbus:modbus-ksp-core")
include(":lib:ksp:metadata:modbus:modbus-ksp-keil-sync")
include(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway")
include(":lib:ksp:metadata:modbus:modbus-ksp-markdown")
include(":lib:ksp:metadata:modbus:modbus-ksp-rtu")
include(":lib:ksp:metadata:modbus:modbus-ksp-rtu-smoke")
include(":lib:ksp:metadata:modbus:modbus-ksp-tcp")
include(":lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin")
include(":lib:ksp:metadata:modbus:modbus-runtime")
include(":lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin")
include(":lib:tool-jvm:tool-modbus")
include(":lib:tool-jvm:tool-serial")
include(":lib:tool-kmp:network-starter")
// <<< Gradle Module Sleep: End Of Block <<<
