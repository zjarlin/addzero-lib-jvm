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
// Generated at: 2026-02-15T18:20:36.283479
// Loaded: 9, Excluded: 0, Total: 9
include(":lib:ksp")
include(":lib:ksp:metadata:method-semanticizer:api")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-api")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-processor")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi-helper")
include(":lib:tool-jvm:network-call:music:api-music-spi")
include(":lib:tool-jvm:network-call:music:api-netease")
include(":lib:tool-jvm:network-call:music:api-netease-semantic-impl")
// <<< Gradle Module Sleep: End Of Block <<<
