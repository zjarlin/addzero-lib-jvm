rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-10T18:02:50.590506
// Loaded: 16, Excluded: 0, Total: 16
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-cupertino-adapter")
include(":lib:compose:compose-crud-spi")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-native-component-table")
include(":lib:compose:compose-native-component-table-core")
include(":lib:compose:compose-native-component-table-pro")
include(":lib:compose:compose-native-component-tree")
include(":lib:gradle-plugin:settings-plugin:gradle-repo-buddy")
include(":lib:ksp:metadata:controller2api-smoke")
include(":lib:ksp:metadata:entity2form:entity2form-processor")
include(":lib:ksp:metadata:entity2iso-processor")
include(":lib:tool-jvm:tool-jvmstr")
include(":lib:tool-jvm:tool-modbus")
include(":lib:tool-jvm:tool-serial")
include(":lib:tool-kmp:tool-model")
// <<< Gradle Module Sleep: End Of Block <<<
