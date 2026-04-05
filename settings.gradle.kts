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

includeBuild(activeBuildLogicDir) {
  name = "addzero-lib-jvm-build-logic"
}

extensions.configure<site.addzero.gradle.GitDependencysExtension>("implementationRemoteGit") {
  if (activeBuildLogicDir.resolve("src/main/kotlin").isDirectory) {
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
  versionCatalogs {
    create("libs") {
      from(files("${activeBuildLogicDir.path}/gradle/libs.versions.toml"))
    }
  }
}

// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-04T22:31:27.940807
// Loaded: 16, Excluded: 0, Total: 16
include(":example:example-kcp-i18n")
include(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-gradle-plugin")
include(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-plugin")
include(":lib:kcp:kcp-i18n")
include(":lib:kcp:kcp-i18n-gradle-plugin")
include(":lib:kcp:kcp-i18n-runtime")
include(":lib:kcp:multireceiver:kcp-multireceiver-annotations")
include(":lib:kcp:multireceiver:kcp-multireceiver-gradle-plugin")
include(":lib:kcp:multireceiver:kcp-multireceiver-plugin")
include(":lib:kcp:singleton-adapter-kcp")
include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin")
include(":lib:kcp:spread-pack:kcp-spread-pack-plugin")
include(":lib:kcp:transform-overload:kcp-transform-overload-annotations")
include(":lib:kcp:transform-overload:kcp-transform-overload-gradle-plugin")
include(":lib:kcp:transform-overload:kcp-transform-overload-plugin")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:kcp:spread-pack:kcp-spread-pack-plugin")
include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin")
if (file("lib/kcp/spread-pack/kcp-spread-pack-ide-plugin").isDirectory) {
  include(":lib:kcp:spread-pack:kcp-spread-pack-ide-plugin")
}
include(":lib:tool-kmp:tool-enum")
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
include(":lib:compose:compose-apple-corner")
include(":lib:compose:compose-cupertino-workbench")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-autocomplet")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-sheet")
include(":lib:compose:compose-native-component-text")
include(":lib:compose:compose-native-component-tree")
include(":lib:compose:compose-native-component-knowledgegraph")
include(":lib:compose:scaffold-spi")
include(":lib:compose:compose-sheet-spi")
include(":lib:compose:shadcn-compose-component")
