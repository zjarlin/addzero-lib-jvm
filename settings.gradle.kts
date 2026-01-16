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
//  id("site.addzero.gradle.plugin.modules-buddy") version "2026.01.11"
}
includeBuild("checkouts/build-logic")
implementationRemoteGit {
//  remoteGits = listOf(
//    "lsi"
////       ,"ddlgenerator"
//  )
}




// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-01-16T16:18:33.256797
// Loaded: 3, Excluded: 0, Total: 3
include(":lib:tool-jvm:network-call:music:tool-api-music-search")
include(":lib:tool-jvm:network-call:music:tool-api-suno")
include(":lib:tool-jvm:network-call:music:tool-music-design")
// <<< Gradle Buddy: End Of Block <<<
