import site.addzero.gradle.RepoType

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "+"
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
// Generated at: 2026-02-03T21:32:02.809451
// Loaded: 2, Excluded: 0, Total: 2
include(":lib:tool-jvm:network-call:music:tool-api-suno")
include(":lib:tool-kmp:models:common:common-models")
// <<< Gradle Module Sleep: End Of Block <<<
