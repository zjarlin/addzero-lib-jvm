pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    google()
  }
  plugins {
    id("site.addzero.kcp.multireceiver") version "2026.03.13"
    id("site.addzero.kcp.transform-overload") version "2026.03.13"
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
// Generated at: 2026-03-15T11:07:43.514768
// Loaded: 11, Excluded: 0, Total: 11
include(":example:example-multireceiver")
include(":example:example-transform-overload")
include(":lib:kcp:multireceiver:kcp-multireceiver-annotations")
include(":lib:kcp:multireceiver:kcp-multireceiver-gradle-plugin")
include(":lib:kcp:multireceiver:kcp-multireceiver-idea-plugin")
include(":lib:kcp:multireceiver:kcp-multireceiver-plugin")
include(":lib:ksp:metadata:multireceiver-processor")
include(":lib:kcp:transform-overload:kcp-transform-overload")
include(":lib:kcp:transform-overload:kcp-transform-overload-annotations")
include(":lib:kcp:transform-overload:kcp-transform-overload-gradle-plugin")
include(":lib:kcp:transform-overload:kcp-transform-overload-plugin")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:ksp:metadata:spring2ktor-server-core")
include(":lib:ksp:metadata:spring2ktor-server-processor")
include(":lib:ksp:metadata:spring2ktor-server-smoke")
