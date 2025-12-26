rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
//    https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
includeBuild("checkouts/build-logic")
//implementationRemoteGit{
//    remoteGits=listOf(
//        ""
//    )
//}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-26T11:16:16.464458
// Loaded: 4, Excluded: 0, Total: 4
include(":lib:gradle-plugin:project-plugin:conventions:jvm-conventions:java-convention")
include(":lib:gradle-plugin:project-plugin:conventions:kmp-conventions:kmp-convention")
include(":lib:gradle-plugin:project-plugin:conventions:koin-convention")
include(":lib:tool-jvm:tool-common-jvm")
// <<< Gradle Buddy: End Of Block <<<
