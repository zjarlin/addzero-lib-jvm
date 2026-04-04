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

// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-04T10:15:17.312724
// Loaded: 28, Excluded: 0, Total: 28
include(":example:example-all-object-jvm-static")
include(":example:example-all-object-jvm-static:java-app")
include(":example:example-all-object-jvm-static:kotlin-lib")
include(":example:example-kcp-i18n")
include(":example:example-multireceiver")
include(":example:example-sheet-workbench")
include(":example:example-spread-pack")
include(":example:example-transform-overload")
include(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-gradle-plugin")
include(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-plugin")
include(":lib:kcp:kcp-i18n")
include(":lib:kcp:kcp-i18n-gradle-plugin")
include(":lib:kcp:kcp-i18n-idea-plugin")
include(":lib:kcp:kcp-i18n-runtime")
include(":lib:kcp:multireceiver:kcp-multireceiver-annotations")
include(":lib:kcp:multireceiver:kcp-multireceiver-gradle-plugin")
include(":lib:kcp:multireceiver:kcp-multireceiver-idea-plugin")
include(":lib:kcp:multireceiver:kcp-multireceiver-plugin")
include(":lib:kcp:singleton-adapter-kcp")
include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin")
include(":lib:kcp:spread-pack:kcp-spread-pack-ide-plugin")
include(":lib:kcp:spread-pack:kcp-spread-pack-plugin")
include(":lib:kcp:transform-overload:kcp-transform-overload")
include(":lib:kcp:transform-overload:kcp-transform-overload-annotations")
include(":lib:kcp:transform-overload:kcp-transform-overload-gradle-plugin")
include(":lib:kcp:transform-overload:kcp-transform-overload-plugin")
include(":lib:ksp:metadata:multireceiver-processor")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:kcp:spread-pack:kcp-spread-pack-annotations")
include(":lib:kcp:spread-pack:kcp-spread-pack-plugin")
include(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin")
include(":lib:kcp:spread-pack:kcp-spread-pack-ide-plugin")
include(":lib:tool-kmp:tool-enum")
include(":lib:tool-kmp:tool-model")
include(":lib:compose:app-sidebar")
include(":lib:compose:app-sidebar-shadcn-adapter")
include(":lib:compose:compose-sheet-spi")
include(":lib:compose:shadcn-compose-component")
