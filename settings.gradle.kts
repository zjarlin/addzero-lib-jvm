pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    google()
  }
  plugins {
    id("site.addzero.kcp.transform-overload") version "2026.03.12"
  }
}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "2026.01.11"
}
includeBuild("build-logic")
// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-03-12T16:14:02.375370
// Loaded: 4, Excluded: 0, Total: 4
include(":example-transform-overload")
include(":lib:kcp:transform-overload:kcp-transform-overload-annotations")
include(":lib:kcp:transform-overload:kcp-transform-overload-gradle-plugin")
include(":lib:kcp:transform-overload:kcp-transform-overload-plugin")
// <<< Gradle Module Sleep: End Of Block <<<
include(":lib:kcp:transform-overload:kcp-transform-overload-annotations")
include(":lib:kcp:transform-overload:kcp-transform-overload-plugin")
include(":lib:kcp:transform-overload:kcp-transform-overload-gradle-plugin")
include(":lib:tool-kmp:tool-coll")
include(":lib:ksp:metadata:spring2ktor-server-core")
include(":lib:ksp:metadata:spring2ktor-server-processor")
include(":lib:ksp:metadata:spring2ktor-server-smoke")
include(":lib:tool-jvm:network-call:tool-api-payment")
