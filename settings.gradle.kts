rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
//    https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations
    id("site.addzero.gradle.plugin.git-dependency") version "2025.12.04"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
implementationRemoteGit{
    remoteGits=listOf(

        "metaprogramming-lsi"
    )
}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-24T17:59:44.325542
// Loaded: 4, Excluded: 0, Total: 4
include(":lib:gradle-plugin:settings-plugin:gradle-git-dependency")
include(":lib:ksp:metadata:ioc:ioc-core")
include(":lib:ksp:metadata:ioc:ioc-processor")
include(":lib:tool-kmp:jdbc:tool-jdbc")
// <<< Gradle Buddy: End Of Block <<<
