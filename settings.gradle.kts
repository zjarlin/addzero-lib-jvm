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
// Generated at: 2026-01-22T10:31:49.784475
// Loaded: 1, Excluded: 0, Total: 1
//include(":lib:ksp:metadata:singleton-adapter-processor") // excluded by Gradle Buddy
// <<< Gradle Buddy: End Of Block <<<
//include(":lib:ksp:metadata:singleton-adapter-api") // excluded by Gradle Buddy
//include(":lib:ksp:metadata:singleton-adapter-processor") // excluded by Gradle Buddy


// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-01-27T11:29:07.622689
// Loaded: 13, Excluded: 0, Total: 13
include(":lib:gradle-plugin:auto-jvmname")
include(":lib:gradle-plugin:gradle-script")
include(":lib:gradle-plugin:gradle-tool")
include(":lib:gradle-plugin:tool-gradle-projectdir")
include(":lib:gradle-plugin:tool:gradle-script")
include(":lib:gradle-plugin:tool:gradle-script-core")
include(":lib:gradle-plugin:tool:gradle-tool-config-java")
include(":lib:gradle-plugin:tool:tool-gradle-projectdir")
include(":lib:tool-jvm:network-call:music:tool-api-music-search")
include(":lib:tool-jvm:network-call:music:tool-api-suno")
include(":lib:tool-jvm:tool-excel")
include(":lib:tool-jvm:tool-yml")
include(":lib:tool-kmp:models:common:common-models")
// <<< Gradle Module Sleep: End Of Block <<<
