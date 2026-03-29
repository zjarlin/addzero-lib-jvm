//pluginManagement {
//  repositories {
//    mavenLocal()
//    gradlePluginPortal()
//    mavenCentral()
//    google()
//  }
//  plugins {
//    id("site.addzero.kcp.i18n") version "2026.10329.10127"
//    id("site.addzero.kcp.multireceiver") version "2026.10329.10127"
//    id("site.addzero.kcp.transform-overload") version "2026.10329.10127"
//  }
//}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}

val localBuildLogicDir = file("checkouts/build-logic")
val sharedKlibBuildLogicDir = file("../addzero-lib-kmp/lib-git/build-logic-klibs")
val activeBuildLogicDir = when {
  localBuildLogicDir.resolve("src/main/kotlin").isDirectory -> localBuildLogicDir
  sharedKlibBuildLogicDir.resolve("src/main/kotlin").isDirectory -> sharedKlibBuildLogicDir
  else -> localBuildLogicDir
}

includeBuild(activeBuildLogicDir) {
  name = "addzero-lib-jvm-build-logic"
}

// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-03-28T13:04:04.118052
// Loaded: 1, Excluded: 0, Total: 1
include(":lib:compose:compose-icon-map")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:ksp:route:route-core")
project(":lib:ksp:route:route-core").projectDir = file("lib/ksp/route/route-core")

include(":lib:ksp:route:route-processor")
project(":lib:ksp:route:route-processor").projectDir = file("lib/ksp/route/route-processor")

include(":lib:kcp:kcp-i18n")
project(":lib:kcp:kcp-i18n").projectDir = file("lib/kcp/kcp-i18n")

include(":lib:kcp:kcp-i18n-runtime")
project(":lib:kcp:kcp-i18n-runtime").projectDir = file("lib/kcp/kcp-i18n-runtime")

include(":lib:kcp:kcp-i18n-gradle-plugin")
project(":lib:kcp:kcp-i18n-gradle-plugin").projectDir = file("lib/kcp/kcp-i18n-gradle-plugin")

include(":lib:kcp:kcp-i18n-idea-plugin")
project(":lib:kcp:kcp-i18n-idea-plugin").projectDir = file("lib/kcp/kcp-i18n-idea-plugin")

include(":lib:compose:compose-zh-fonts")
project(":lib:compose:compose-zh-fonts").projectDir = file("lib/compose/compose-zh-fonts")

include(":lib:ksp:metadata:modbus:modbus-runtime")
project(":lib:ksp:metadata:modbus:modbus-runtime").projectDir = file("lib/ksp/metadata/modbus/modbus-runtime")

include(":lib:ksp:metadata:modbus:modbus-ksp-core")
project(":lib:ksp:metadata:modbus:modbus-ksp-core").projectDir = file("lib/ksp/metadata/modbus/modbus-ksp-core")

include(":lib:ksp:metadata:modbus:modbus-ksp-rtu")
project(":lib:ksp:metadata:modbus:modbus-ksp-rtu").projectDir = file("lib/ksp/metadata/modbus/modbus-ksp-rtu")

include(":lib:ksp:metadata:modbus:modbus-ksp-tcp")
project(":lib:ksp:metadata:modbus:modbus-ksp-tcp").projectDir = file("lib/ksp/metadata/modbus/modbus-ksp-tcp")

include(":lib:ksp:metadata:controller2api-processor")
project(":lib:ksp:metadata:controller2api-processor").projectDir = file("lib/ksp/metadata/controller2api-processor")

include(":lib:ksp:metadata:spring2ktor-server-core")
project(":lib:ksp:metadata:spring2ktor-server-core").projectDir = file("lib/ksp/metadata/spring2ktor-server-core")

include(":lib:ksp:metadata:spring2ktor-server-processor")
project(":lib:ksp:metadata:spring2ktor-server-processor").projectDir = file("lib/ksp/metadata/spring2ktor-server-processor")
