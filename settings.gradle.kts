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
// Generated at: 2026-04-03T17:16:03.356685
// Loaded: 38, Excluded: 1, Total: 39
// Excluded (build infrastructure): :checkouts:build-logic
include(":lib:compose:app-sidebar")
include(":lib:compose:compose-crud-spi")
include(":lib:compose:compose-eventbus")
include(":lib:compose:compose-icon-map")
include(":lib:compose:compose-klibs-component")
include(":lib:compose:compose-model-component")
include(":lib:compose:compose-native-component-assist")
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-card")
include(":lib:compose:compose-native-component-ext")
include(":lib:compose:compose-native-component-form")
include(":lib:compose:compose-native-component-glass")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-hook")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-select")
include(":lib:compose:compose-native-component-table")
include(":lib:compose:compose-native-component-table-core")
include(":lib:compose:compose-native-component-table-pro")
include(":lib:compose:compose-native-component-text")
include(":lib:compose:compose-native-component-toast")
include(":lib:compose:compose-native-component-tree")
include(":lib:compose:compose-zh-fonts")
include(":lib:compose:glass-components")
include(":lib:compose:liquid-glass")
include(":lib:compose:media-playlist-player")
include(":lib:compose:scaffold-spi")
include(":lib:compose:shadcn-compose-component")
include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base")
include(":lib:ksp:metadata:compose-props:compose-props-annotations")
include(":lib:ksp:metadata:compose-props:compose-props-gradle-plugin")
include(":lib:ksp:metadata:compose-props:compose-props-processor")
include(":lib:lsi:lsi-core")
include(":lib:lsi:lsi-ksp")
include(":lib:tool-jvm:tool-serial")
include(":lib:tool-jvm:tool-stm32-bootloader")
include(":lib:tool-kmp:tool-tree")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:kcp:spread-pack:kcp-spread-pack-plugin")
include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin")
include(":lib:lsi:lsi-core")
include(":lib:lsi:lsi-ksp")

// Compose 自包含模块先恢复到项目图，便于分批编译和重构验证。
include(":lib:compose:app-sidebar")
include(":lib:compose:compose-icon-map")
include(":lib:compose:compose-klibs-component")
include(":lib:compose:compose-model-component")
include(":lib:compose:compose-native-component-assist")
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-card")
include(":lib:compose:compose-native-component-ext")
include(":lib:compose:compose-native-component-form")
include(":lib:compose:compose-native-component-glass")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-hook")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-select")
include(":lib:compose:compose-native-component-table")
include(":lib:compose:compose-native-component-table-core")
include(":lib:compose:compose-native-component-table-pro")
include(":lib:compose:compose-native-component-text")
include(":lib:compose:compose-native-component-toast")
include(":lib:compose:compose-native-component-tree")
include(":lib:compose:compose-zh-fonts")
include(":lib:compose:glass-components")
include(":lib:compose:liquid-glass")
include(":lib:compose:media-playlist-player")
include(":lib:compose:scaffold-spi")
include(":lib:compose:shadcn-compose-component")

// compose-props 消费插件声明了本地兜底路径，同仓库验证时需要一并纳入项目图。
include(":lib:ksp:metadata:compose-props:compose-props-annotations")
include(":lib:ksp:metadata:compose-props:compose-props-processor")
include(":lib:ksp:metadata:compose-props:compose-props-gradle-plugin")
