rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-10T10:13:08.023254
// Loaded: 16, Excluded: 0, Total: 16
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-cupertino-adapter")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-tree")
include(":lib:compose:compose-workbench-design")
include(":lib:compose:scaffold-spi")
include(":lib:compose:shadcn-compose-component")
include(":lib:ksp:metadata:modbus:modbus-ksp-c-contract")
include(":lib:ksp:metadata:modbus:modbus-ksp-core")
include(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway")
include(":lib:ksp:metadata:modbus:modbus-ksp-markdown")
include(":lib:tool-jvm:database:tool-sql-executor")
include(":lib:tool-kmp:tool-tree")
// <<< Gradle Module Sleep: End Of Block <<<
