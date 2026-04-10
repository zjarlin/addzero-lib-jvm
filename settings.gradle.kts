rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-10T10:39:09.327288
// Loaded: 12, Excluded: 0, Total: 12
include(":lib:api:api-netease")
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-cupertino-adapter")
include(":lib:compose:app-sidebar-shadcn-adapter")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-eventbus")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-toast")
include(":lib:compose:compose-native-component-tree")
include(":lib:tool-jvm:tool-modbus")
include(":lib:tool-kmp:tool-tree")
// <<< Gradle Module Sleep: End Of Block <<<
