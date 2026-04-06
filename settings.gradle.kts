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
// Generated at: 2026-04-06T15:53:59.729467
// Loaded: 8, Excluded: 0, Total: 8
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-chat")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-text")
include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin")
include(":lib:tool-jvm:tool-serial")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
//include(":lib:kcp:spread-pack:kcp-spread-pack-plugin") // excluded by Gradle Buddy
include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin")
if (file("lib/kcp/spread-pack/kcp-spread-pack-ide-plugin").isDirectory) {
//  include(":lib:kcp:spread-pack:kcp-spread-pack-ide-plugin") // excluded by Gradle Buddy
}
//include(":lib:tool-kmp:tool-enum") // excluded by Gradle Buddy
//include(":lib:tool-kmp:network-starter") // excluded by Gradle Buddy
//include(":lib:tool-kmp:tool-koin") // excluded by Gradle Buddy
//include(":lib:tool-kmp:tool-model") // excluded by Gradle Buddy
//include(":lib:tool-kmp:tool-regex") // excluded by Gradle Buddy
//include(":lib:tool-kmp:tool-str") // excluded by Gradle Buddy
//include(":lib:tool-kmp:tool-tree") // excluded by Gradle Buddy
//include(":lib:compose:app-sidebar") // excluded by Gradle Buddy
//include(":lib:compose:app-sidebar-cupertino-adapter") // excluded by Gradle Buddy
//include(":lib:compose:app-sidebar-shadcn-adapter") // excluded by Gradle Buddy
if (file("lib/compose/compose-workbench-design").isDirectory) {
//include(":lib:compose:compose-workbench-design") // excluded by Gradle Buddy
}
if (file("lib/compose/compose-workbench-shell").isDirectory) {
//include(":lib:compose:compose-workbench-shell") // excluded by Gradle Buddy
}
if (file("lib/compose/compose-workbench-immersive-desktop").isDirectory) {
//include(":lib:compose:compose-workbench-immersive-desktop") // excluded by Gradle Buddy
}
//include(":lib:compose:compose-apple-corner") // excluded by Gradle Buddy
//include(":lib:compose:compose-crud-spi") // excluded by Gradle Buddy
//include(":lib:compose:compose-cupertino-workbench") // excluded by Gradle Buddy
//include(":lib:compose:compose-eventbus") // excluded by Gradle Buddy
//include(":lib:compose:compose-icon-map") // excluded by Gradle Buddy
//include(":lib:compose:compose-klibs-component") // excluded by Gradle Buddy
//include(":lib:compose:compose-model-component") // excluded by Gradle Buddy
include(":lib:compose:compose-native-component-high-level")
//include(":lib:compose:compose-native-component-assist") // excluded by Gradle Buddy
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-native-component-button")
//include(":lib:compose:compose-native-component-card") // excluded by Gradle Buddy
include(":lib:compose:compose-native-component-chat")
//include(":lib:compose:compose-native-component-ext") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-form") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-glass") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-hook") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-searchbar") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-select") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-sheet") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-table-core") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-table") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-table-pro") // excluded by Gradle Buddy
include(":lib:compose:compose-native-component-text")
//include(":lib:compose:compose-native-component-toast") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-tree") // excluded by Gradle Buddy
//include(":lib:compose:compose-native-component-knowledgegraph") // excluded by Gradle Buddy
//include(":lib:compose:scaffold-spi") // excluded by Gradle Buddy
//include(":lib:compose:compose-sheet-spi") // excluded by Gradle Buddy
//include(":lib:compose:compose-zh-fonts") // excluded by Gradle Buddy
//include(":lib:compose:glass-components") // excluded by Gradle Buddy
//include(":lib:compose:liquid-glass") // excluded by Gradle Buddy
//include(":lib:compose:media-playlist-player") // excluded by Gradle Buddy
//include(":lib:compose:shadcn-compose-component") // excluded by Gradle Buddy

if (file("lib/gradle-plugin/project-plugin/gradle-ksp-consumer-base").isDirectory) {
//  include(":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base") // excluded by Gradle Buddy
}
if (file("lib/tool-jvm/tool-modbus").isDirectory) {
//  include(":lib:tool-jvm:tool-modbus") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-ksp-core").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-ksp-core") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-ksp-kotlin-gateway").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-ksp-c-contract").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-ksp-c-contract") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-ksp-keil-sync").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-ksp-keil-sync") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-ksp-markdown").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-ksp-markdown") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-ksp-rtu").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-ksp-rtu") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-ksp-tcp").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-ksp-tcp") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-runtime").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-runtime") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-rtu-gradle-plugin").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-tcp-gradle-plugin").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-tcp-gradle-plugin") // excluded by Gradle Buddy
}
if (file("lib/ksp/metadata/modbus/modbus-ksp-rtu-smoke").isDirectory) {
//  include(":lib:ksp:metadata:modbus:modbus-ksp-rtu-smoke") // excluded by Gradle Buddy
}
