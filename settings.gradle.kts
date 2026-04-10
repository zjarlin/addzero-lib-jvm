rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-10T10:26:19.084515
// Loaded: 42, Excluded: 0, Total: 42
include(":lib:api:api-netease")
include(":lib:compose:compose-apple-corner")
include(":lib:compose:compose-crud-spi")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-eventbus")
include(":lib:compose:compose-icon-keys")
include(":lib:compose:compose-icon-map")
include(":lib:compose:compose-klibs-component")
include(":lib:compose:compose-model-component")
include(":lib:compose:compose-native-component-assist")
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-card")
include(":lib:compose:compose-native-component-chat")
include(":lib:compose:compose-native-component-ext")
include(":lib:compose:compose-native-component-form")
include(":lib:compose:compose-native-component-glass")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-hook")
include(":lib:compose:compose-native-component-knowledgegraph")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-select")
include(":lib:compose:compose-native-component-sheet")
include(":lib:compose:compose-native-component-table")
include(":lib:compose:compose-native-component-table-core")
include(":lib:compose:compose-native-component-table-pro")
include(":lib:compose:compose-native-component-text")
include(":lib:compose:compose-native-component-toast")
include(":lib:compose:compose-native-component-tree")
include(":lib:compose:compose-sheet-spi")
include(":lib:compose:compose-workbench-design")
include(":lib:compose:compose-workbench-immersive-desktop")
include(":lib:compose:compose-workbench-shell")
include(":lib:compose:compose-zh-fonts")
include(":lib:compose:scaffold-spi")
include(":lib:compose:shadcn-compose-component")
include(":lib:ksp:metadata:compose-props:compose-props-annotations")
include(":lib:ksp:metadata:compose-props:compose-props-gradle-plugin")
include(":lib:ksp:metadata:compose-props:compose-props-processor")
include(":lib:tool-jvm:tool-modbus")
include(":lib:tool-kmp:tool-model")
include(":lib:tool-kmp:tool-tree")
// <<< Gradle Module Sleep: End Of Block <<<
