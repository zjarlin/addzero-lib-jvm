rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-14T14:39:20.388057
// Loaded: 11, Excluded: 0, Total: 11
include(":lib:ksp:metadata:modbus:modbus-codegen-core")
include(":lib:ksp:metadata:modbus:modbus-ksp-c-contract")
include(":lib:ksp:metadata:modbus:modbus-ksp-core")
include(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway")
include(":lib:ksp:metadata:modbus:modbus-ksp-mqtt")
include(":lib:ksp:metadata:modbus:modbus-ksp-rtu")
include(":lib:ksp:metadata:modbus:modbus-mqtt-gradle-plugin")
include(":lib:ksp:metadata:modbus:modbus-runtime")
include(":lib:ksp:route:route-processor")
include(":lib:tool-jvm:tool-modbus")
include(":lib:tool-jvm:tool-serial")
// <<< Gradle Module Sleep: End Of Block <<<
