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
// Generated at: 2026-04-06T08:36:13.265316
// Loaded: 57, Excluded: 0, Total: 57
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-cupertino-adapter")
include(":lib:compose:compose-apple-corner")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-text")
include(":lib:compose:compose-native-component-tree")
include(":lib:compose:compose-workbench-design")
include(":lib:compose:scaffold-spi")
include(":lib:compose:shadcn-compose-component")
include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base")
include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:ksp:metadata:controller2feign-processor")
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
include(":lib:tool-jvm:database:tool-sql")
include(":lib:tool-jvm:database:tool-sql-executor")
include(":lib:tool-jvm:database:tool-sql-injection")
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
include(":lib:tool-jvm:tool-context")
include(":lib:tool-jvm:tool-modbus")
include(":lib:tool-jvm:tool-serial")
include(":lib:tool-kmp:network-starter")
include(":lib:tool-kmp:tool-coll")
include(":lib:tool-kmp:tool-tree")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
//include(":lib:kcp:spread-pack:kcp-spread-pack-plugin") // excluded by Gradle Buddy
//include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin") // excluded by Gradle Buddy
if (file("lib/kcp/spread-pack/kcp-spread-pack-ide-plugin").isDirectory) {
//  include(":lib:kcp:spread-pack:kcp-spread-pack-ide-plugin") // excluded by Gradle Buddy
}
//include(":lib:tool-kmp:tool-enum") // excluded by Gradle Buddy
include(":lib:tool-kmp:network-starter")
//include(":lib:tool-kmp:tool-koin") // excluded by Gradle Buddy
//include(":lib:tool-kmp:tool-model") // excluded by Gradle Buddy
//include(":lib:tool-kmp:tool-regex") // excluded by Gradle Buddy
//include(":lib:tool-kmp:tool-str") // excluded by Gradle Buddy
include(":lib:tool-kmp:tool-tree")
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-cupertino-adapter")
//include(":lib:compose:app-sidebar-shadcn-adapter") // excluded by Gradle Buddy
if (file("lib/compose/compose-workbench-design").isDirectory) {
  include(":lib:compose:compose-workbench-design")
}
if (file("lib/compose/compose-workbench-shell").isDirectory) {
//  include(":lib:compose:compose-workbench-shell") // excluded by Gradle Buddy
}
if (file("lib/compose/compose-workbench-immersive-desktop").isDirectory) {
//  include(":lib:compose:compose-workbench-immersive-desktop") // excluded by Gradle Buddy
}
include(":lib:compose:compose-apple-corner")
//include(":lib:compose:compose-crud-spi") // excluded by Gradle Buddy
include(":lib:compose:compose-cupertino-workbench")
//include(":lib:compose:compose-eventbus") // excluded by Gradle Buddy
//include(":lib:compose:compose-icon-map") // excluded by Gradle Buddy
//include(":lib:compose:compose-klibs-component") // excluded by Gradle Buddy
//include(":lib:compose:compose-model-component") // excluded by Gradle Buddy
include(":lib:compose:compose-native-component-high-level")
//include(":lib:compose:compose-native-component-assist") // excluded by Gradle Buddy
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-native-component-button")
//include(":lib:compose:compose-native-component-card") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-chat") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-ext") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-form") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-glass") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-hook") // excluded by Gradle Buddy
include(":lib:compose:compose-native-component-searchbar")
//include(":lib:compose:compose-native-component-select") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-sheet") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-table-core") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-table") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-table-pro") // excluded by Gradle Buddy
include(":lib:compose:compose-native-component-text")
//include(":lib:compose:compose-native-component-toast") // excluded by Gradle Buddy
include(":lib:compose:compose-native-component-tree")
//include(":lib:compose:compose-native-component-knowledgegraph") // excluded by Gradle Buddy
include(":lib:compose:scaffold-spi")
//include(":lib:compose:compose-sheet-spi") // excluded by Gradle Buddy
//include(":lib:compose:compose-zh-fonts") // excluded by Gradle Buddy
//include(":lib:compose:glass-components") // excluded by Gradle Buddy
//include(":lib:compose:liquid-glass") // excluded by Gradle Buddy
//include(":lib:compose:media-playlist-player") // excluded by Gradle Buddy
include(":lib:compose:shadcn-compose-component")
