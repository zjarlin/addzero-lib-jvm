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
// Generated at: 2025-12-25T21:24:20.427621
// Loaded: 3, Excluded: 0, Total: 3
include(":lib:gradle-plugin:project-plugin:gradle-processor-buddy")
include(":lib:ksp:metadata:ioc:ioc-core")
include(":lib:ksp:metadata:ioc:ioc-processor")
// <<< Gradle Buddy: End Of Block <<<
