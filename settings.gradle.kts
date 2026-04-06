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
// Generated at: 2026-04-06T15:18:49.975090
// Loaded: 56, Excluded: 0, Total: 56
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-cupertino-adapter")
include(":lib:compose:app-sidebar-shadcn-adapter")
include(":lib:compose:compose-apple-corner")
include(":lib:compose:compose-crud-spi")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-eventbus")
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
include(":lib:compose:glass-components")
include(":lib:compose:liquid-glass")
include(":lib:compose:media-playlist-player")
include(":lib:compose:scaffold-spi")
include(":lib:compose:shadcn-compose-component")
include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base")
include(":lib:gradle-plugin:project-plugin:gradle-publish-budy")
include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:ksp:metadata:compose-props:compose-props-annotations")
include(":lib:ksp:metadata:compose-props:compose-props-gradle-plugin")
include(":lib:ksp:metadata:compose-props:compose-props-processor")
include(":lib:lsi:lsi-core")
include(":lib:lsi:lsi-ksp")
include(":lib:tool-kmp:network-starter")
include(":lib:tool-kmp:tool-coll")
include(":lib:tool-kmp:tool-enum")
include(":lib:tool-kmp:tool-koin")
include(":lib:tool-kmp:tool-model")
include(":lib:tool-kmp:tool-regex")
include(":lib:tool-kmp:tool-str")
include(":lib:tool-kmp:tool-tree")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
//include(":lib:kcp:spread-pack:kcp-spread-pack-plugin") // excluded by Gradle Buddy
//include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin") // excluded by Gradle Buddy
if (file("lib/kcp/spread-pack/kcp-spread-pack-ide-plugin").isDirectory) {
//  include(":lib:kcp:spread-pack:kcp-spread-pack-ide-plugin") // excluded by Gradle Buddy
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
