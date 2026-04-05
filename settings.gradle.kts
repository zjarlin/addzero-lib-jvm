pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    google()
  }
  plugins {
    id("site.addzero.kcp.all-object-jvm-static") version "+"
    id("site.addzero.kcp.i18n") version "+"
    id("site.addzero.kcp.multireceiver") version "+"
    id("site.addzero.kcp.transform-overload") version "+"
    id("site.addzero.kcp.spread-pack") version "+"
  }
}

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
val hasActiveBuildLogicDir = activeBuildLogicDir.resolve("src/main/kotlin").isDirectory
val activeBuildLogicCatalogFile = activeBuildLogicDir.resolve("gradle/libs.versions.toml")

if (hasActiveBuildLogicDir) {
  includeBuild(activeBuildLogicDir) {
    name = "addzero-lib-jvm-build-logic"
  }
}

extensions.configure<site.addzero.gradle.GitDependencysExtension>("implementationRemoteGit") {
  if (hasActiveBuildLogicDir) {
    enableZlibs.set(false)
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
  repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
  }
  if (activeBuildLogicCatalogFile.isFile) {
    versionCatalogs {
      create("libs") {
        from(files(activeBuildLogicCatalogFile))
      }
    }
  }
}

// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-05T16:37:55.020205
// Loaded: 49, Excluded: 0, Total: 49
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-cupertino-adapter")
include(":lib:compose:compose-apple-corner")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-tree")
include(":lib:compose:compose-workbench-design")
include(":lib:compose:scaffold-spi")
include(":lib:compose:shadcn-compose-component")
include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi-helper")
include(":lib:ksp:metadata:modbus:modbus-ksp-c-contract")
include(":lib:ksp:metadata:modbus:modbus-ksp-core")
include(":lib:ksp:metadata:modbus:modbus-ksp-keil-sync")
include(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway")
include(":lib:ksp:metadata:modbus:modbus-ksp-markdown")
include(":lib:ksp:metadata:modbus:modbus-ksp-rtu")
include(":lib:ksp:metadata:modbus:modbus-ksp-rtu-smoke")
include(":lib:ksp:metadata:modbus:modbus-ksp-tcp")
include(":lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin")
include(":lib:ksp:metadata:modbus:modbus-runtime")
include(":lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin")
include(":lib:tool-jvm:network-call:browser:tool-api-browser-automation")
include(":lib:tool-jvm:network-call:browser:ws-automation")
include(":lib:tool-jvm:network-call:chat:api-models-chat")
include(":lib:tool-jvm:network-call:music:api-music-spi")
include(":lib:tool-jvm:network-call:music:api-netease")
include(":lib:tool-jvm:network-call:music:api-netease-semantic-impl")
include(":lib:tool-jvm:network-call:music:tool-api-music-search")
include(":lib:tool-jvm:network-call:music:tool-api-suno")
include(":lib:tool-jvm:network-call:music:tool-music-design")
include(":lib:tool-jvm:network-call:tool-api-maven")
include(":lib:tool-jvm:network-call:tool-api-ocr")
include(":lib:tool-jvm:network-call:tool-api-payment")
include(":lib:tool-jvm:network-call:tool-api-soft-download")
include(":lib:tool-jvm:network-call:tool-api-temp-mail")
include(":lib:tool-jvm:network-call:tool-api-translate")
include(":lib:tool-jvm:network-call:tool-api-tyc")
include(":lib:tool-jvm:network-call:tool-api-tyc-hw")
include(":lib:tool-jvm:network-call:tool-api-video-parse")
include(":lib:tool-jvm:network-call:tool-api-video-search-and-download")
include(":lib:tool-jvm:network-call:tool-api-weather")
include(":lib:tool-jvm:tool-modbus")
include(":lib:tool-jvm:tool-serial")
include(":lib:tool-kmp:network-starter")
include(":lib:tool-kmp:tool-coll")
include(":lib:tool-kmp:tool-tree")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:kcp:spread-pack:kcp-spread-pack-plugin")
include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin")
if (file("lib/kcp/spread-pack/kcp-spread-pack-ide-plugin").isDirectory) {
  include(":lib:kcp:spread-pack:kcp-spread-pack-ide-plugin")
}
include(":lib:tool-kmp:tool-enum")
include(":lib:tool-kmp:network-starter")
include(":lib:tool-kmp:tool-koin")
include(":lib:tool-kmp:tool-model")
include(":lib:tool-kmp:tool-regex")
include(":lib:tool-kmp:tool-str")
include(":lib:tool-kmp:tool-tree")
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-cupertino-adapter")
include(":lib:compose:app-sidebar-shadcn-adapter")
if (file("lib/compose/compose-workbench-design").isDirectory) {
  include(":lib:compose:compose-workbench-design")
}
if (file("lib/compose/compose-workbench-shell").isDirectory) {
  include(":lib:compose:compose-workbench-shell")
}
if (file("lib/compose/compose-workbench-immersive-desktop").isDirectory) {
  include(":lib:compose:compose-workbench-immersive-desktop")
}
include(":lib:compose:compose-apple-corner")
include(":lib:compose:compose-crud-spi")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-eventbus")
include(":lib:compose:compose-icon-map")
include(":lib:compose:compose-klibs-component")
include(":lib:compose:compose-model-component")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-assist")
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-card")
include(":lib:compose:compose-native-component-chat")
include(":lib:compose:compose-native-component-ext")
include(":lib:compose:compose-native-component-form")
include(":lib:compose:compose-native-component-glass")
include(":lib:compose:compose-native-component-hook")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-select")
include(":lib:compose:compose-native-component-sheet")
include(":lib:compose:compose-native-component-table-core")
include(":lib:compose:compose-native-component-table")
include(":lib:compose:compose-native-component-table-pro")
include(":lib:compose:compose-native-component-text")
include(":lib:compose:compose-native-component-toast")
include(":lib:compose:compose-native-component-tree")
include(":lib:compose:compose-native-component-knowledgegraph")
include(":lib:compose:scaffold-spi")
include(":lib:compose:compose-sheet-spi")
include(":lib:compose:compose-zh-fonts")
include(":lib:compose:glass-components")
include(":lib:compose:liquid-glass")
include(":lib:compose:media-playlist-player")
include(":lib:compose:shadcn-compose-component")
