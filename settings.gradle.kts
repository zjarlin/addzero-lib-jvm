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
// Generated at: 2025-12-17T15:28:03.253575
// Loaded: 3, Excluded: 0, Total: 3
//include(":lib:gradle-plugin:project-plugin:conventions:jvm-conventions:ksp-convention") // excluded by Gradle Buddy
include(":lib:ksp:serviceloader-demo:processor")
include(":lib:ksp:serviceloader-demo:test")
// <<< Gradle Buddy: End Of Block <<<

// ServiceLoader Demo Consumer Module
include(":lib:ksp:serviceloader-demo:consumer")