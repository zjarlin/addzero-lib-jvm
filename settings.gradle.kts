rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-16T08:49:04.383227
// Loaded: 10, Excluded: 0, Total: 10
include(":example:modbus:modbus-ksp-rtu-smoke")
include(":lib:gradle-plugin:project-plugin:gradle-processor-buddy")
include(":lib:ksp:metadata:modbus:modbus-codegen-core")
include(":lib:ksp:metadata:modbus:modbus-codegen-model")
include(":lib:ksp:metadata:modbus:modbus-ksp-c-contract")
include(":lib:ksp:metadata:modbus:modbus-ksp-core")
include(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-contract")
include(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway")
include(":lib:ksp:metadata:modbus:modbus-ksp-rtu")
include(":lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin")
// <<< Gradle Module Sleep: End Of Block <<<
