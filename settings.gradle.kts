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
// Generated at: 2026-01-15T17:18:02.999157
// Loaded: 6, Excluded: 0, Total: 6
include(":lib:gradle-plugin:project-plugin:gradle-processor-buddy")
include(":lib:gradle-plugin:settings-plugin:gradle-modules-buddy")
include(":lib:tool-jvm:database:ddlgenerator")
include(":lib:tool-jvm:network-call:tool-api-maven")
include(":lib:tool-jvm:network-call:tool-api-music-search")
include(":lib:tool-jvm:network-call:tool-api-suno")
// <<< Gradle Buddy: End Of Block <<<
