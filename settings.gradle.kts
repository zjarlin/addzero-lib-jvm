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
includeBuild("checkouts/build-logic")

// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-03-26T19:28:33.453121800
// Loaded: 9, Excluded: 0, Total: 9
include(":example:example-all-object-jvm-static")
include(":example:example-all-object-jvm-static:java-app")
include(":example:example-all-object-jvm-static:kotlin-lib")
include(":example:example-kcp-i18n")
include(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-gradle-plugin")
include(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-plugin")
include(":lib:kcp:multireceiver:kcp-multireceiver-annotations")
include(":lib:ksp:metadata:multireceiver-processor")
include(":lib:tool-jvm:json2kotlin-dataclass")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:ksp:metadata:openapi:openapi-core")
include(":lib:ksp:metadata:openapi:openapi-spring-extractor")
include(":lib:ksp:metadata:openapi:openapi-ktor-extractor")
