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
// Generated at: 2025-12-17T15:52:40.125389
// Loaded: 4, Excluded: 0, Total: 4
include(":lib:ksp:serviceloader-demo:consumer")
include(":lib:ksp:serviceloader-demo:processor")
include(":lib:ksp:serviceloader-demo:service-impls")
include(":lib:ksp:serviceloader-demo:test")
// <<< Gradle Buddy: End Of Block <<<

// ServiceLoader Demo Modules
include(":lib:ksp:serviceloader-demo:consumer")
include(":lib:ksp:serviceloader-demo:service-impls")
