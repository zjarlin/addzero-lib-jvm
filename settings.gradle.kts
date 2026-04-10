rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-10T15:52:24.799583
// Loaded: 14, Excluded: 0, Total: 14
include(":lib:apt:apt-controller2feign-processor")
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-cupertino-adapter")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-native-component-tree")
include(":lib:gradle-plugin:settings-plugin:gradle-repo-buddy")
include(":lib:ksp:metadata:controller2api-idea-plugin")
include(":lib:ksp:metadata:controller2api-processor")
include(":lib:ksp:metadata:controller2api-smoke")
include(":lib:ksp:metadata:controller2feign-processor")
include(":lib:ksp:metadata:controller2iso2dataprovider-processor")
include(":lib:tool-jvm:tool-jvmstr")
include(":lib:tool-jvm:tool-modbus")
include(":lib:tool-jvm:tool-serial")
// <<< Gradle Module Sleep: End Of Block <<<
