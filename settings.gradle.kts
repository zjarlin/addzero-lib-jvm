//pluginManagement {
//  repositories {
//    mavenLocal()
//    gradlePluginPortal()
//    mavenCentral()
//    google()
//  }
//  plugins {
//    id("site.addzero.kcp.i18n") version "2026.03.13"
//    id("site.addzero.kcp.multireceiver") version "2026.03.13"
//    id("site.addzero.kcp.transform-overload") version "2026.03.13"
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

includeBuild(activeBuildLogicDir)

// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-03-27T09:53:21.970828
// Loaded: 9, Excluded: 0, Total: 9
include(":lib:apt:dict-trans:apt-dict-trans-processor")
include(":lib:compose:compose-native-component-button")
include(":lib:compose:compose-native-component-high-level")
include(":lib:compose:compose-native-component-searchbar")
include(":lib:compose:compose-native-component-tree")
include(":lib:gradle-plugin:project-plugin:gradle-processor-buddy")
include(":lib:ksp:route:route-processor")
include(":lib:tool-jvm:network-call:browser:tool-api-browser-automation")
include(":lib:tool-jvm:tool-io")
// <<< Gradle Module Sleep: End Of Block <<<

if (file("lib/tool-kmp/tool-tree/build.gradle.kts").isFile) {
  include(":lib:tool-kmp:tool-tree")
}
