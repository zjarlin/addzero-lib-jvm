import site.addzero.gradle.RepoType

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
//  id("org.gradle.toolchains.foojay-resolver-convention") version "+"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
//    https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "2026.01.11"
}
implementationRemoteGit{
    repoType = RepoType.GITEE
    branch = "master"

}
// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-02-10T17:18:27.515994
// Loaded: 14, Excluded: 0, Total: 14
include(":lib:decompile:huawei-java-sdk")
include(":lib:gradle-plugin:auto-jvmname")
include(":lib:gradle-plugin:settings-plugin:gradle-repo-buddy")
include(":lib:ksp:common:ksp-easycode")
include(":lib:ksp:metadata:controller2api-processor")
include(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-processor")
include(":lib:tool-jvm:database:tool-cte")
include(":lib:tool-jvm:jimmer:jimmer-ext-dynamic-datasource")
include(":lib:tool-jvm:network-call:music:tool-api-music-search")
include(":lib:tool-jvm:network-call:music:tool-api-suno")
include(":lib:tool-jvm:network-call:tool-api-tyc-hw")
include(":lib:tool-kmp:ktor:ktor-banner")
include(":lib:tool-kmp:models:common:common-models")
include(":lib:tool-kmp:network-starter")
// <<< Gradle Module Sleep: End Of Block <<<
