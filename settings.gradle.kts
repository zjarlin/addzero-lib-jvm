// 配置插件解析以使用 Maven 本地仓库
pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "+"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
//    https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
  id("site.addzero.gradle.plugin.modules-buddy") version "2026.01.11"
}
includeBuild("checkouts/build-logic")
implementationRemoteGit {
//  remoteGits = listOf(
//    "lsi"
////       ,"ddlgenerator"
//  )
}




// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-01-22T10:31:49.784475
// Loaded: 1, Excluded: 0, Total: 1
//include(":lib:ksp:metadata:singleton-adapter-processor") // excluded by Gradle Buddy
// <<< Gradle Buddy: End Of Block <<<
//include(":lib:ksp:metadata:singleton-adapter-api") // excluded by Gradle Buddy
//include(":lib:ksp:metadata:singleton-adapter-processor") // excluded by Gradle Buddy


// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-01-22T20:37:12.527478
// Loaded: 16, Excluded: 0, Total: 16
include(":lib:compose:compose-native-component-glass")
include(":lib:gradle-plugin:gradle-tool")
include(":lib:gradle-plugin:project-plugin:conventions:jvm-conventions:koin-convention")
include(":lib:gradle-plugin:project-plugin:conventions:jvm-conventions:ksp-convention")
include(":lib:kcp:multireceiver:kcp-multireceiver-annotations")
include(":lib:ksp:common:ksp-support-jdbc")
include(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-processor")
include(":lib:lsi:lsi-ksp")
include(":lib:tool-jvm:database:tool-sql-executor")
include(":lib:tool-jvm:network-call:music:tool-api-music-search")
include(":lib:tool-jvm:network-call:music:tool-api-suno")
include(":lib:tool-jvm:network-call:music:tool-music-design")
include(":lib:tool-kmp:network-starter")
include(":lib:tool-kmp:tool-expect")
include(":lib:tool-kmp:tool-json")
include(":lib:tool-kmp:tool1")
// <<< Gradle Module Sleep: End Of Block <<<
