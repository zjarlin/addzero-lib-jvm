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
// Generated at: 2026-02-15T14:26:57.097762
// Loaded: 8, Excluded: 0, Total: 8
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-api")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-processor")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi-impl")
include(":lib:tool-jvm:network-call:music:api-music-spi")
include(":lib:tool-jvm:network-call:music:api-netease")
// <<< Gradle Module Sleep: End Of Block <<<
