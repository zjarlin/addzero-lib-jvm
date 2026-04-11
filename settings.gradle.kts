rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-13T10:25:17.946030
// Loaded: 26, Excluded: 0, Total: 26
include(":lib:ksp:metadata:controller2api-smoke")
include(":lib:ksp:metadata:entity2form:entity2form-processor")
include(":lib:ksp:metadata:modbus:modbus-ksp-core")
include(":lib:ksp:metadata:modbus:modbus-ksp-rtu")
include(":lib:ksp:metadata:modbus:modbus-ksp-rtu-smoke")
include(":lib:ksp:metadata:modbus:modbus-ksp-tcp")
include(":lib:ksp:metadata:modbus:modbus-mqtt-gradle-plugin")
include(":lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin")
include(":lib:ksp:metadata:modbus:modbus-runtime")
include(":lib:tool-jvm:database:tool-sql")
include(":lib:tool-jvm:database:tool-sql-executor")
include(":lib:tool-jvm:database:tool-sql-injection")
include(":lib:tool-jvm:tool-bean")
include(":lib:tool-jvm:tool-modbus")
include(":lib:tool-jvm:tool-s3")
include(":lib:tool-jvm:tool-serial")
include(":lib:tool-jvm:tool-spctx")
include(":lib:tool-jvm:tool-spel")
include(":lib:tool-jvm:tool-spring")
include(":lib:tool-jvm:tool-ssh")
include(":lib:tool-jvm:tool-stm32-bootloader")
include(":lib:tool-kmp:tool-str")
include(":lib:tool-starter:controller-advice-spring-boot-starter")
include(":lib:tool-starter:controller-autoconfigure")
include(":lib:tool-starter:curllog-spring-boot-starter")
include(":lib:tool-starter:dict-trans-spring-boot-starter")
// <<< Gradle Module Sleep: End Of Block <<<
