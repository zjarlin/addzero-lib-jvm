rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.git-dependency") version "+"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
implementationRemoteGit{
    remoteGits=listOf("addzero-lib-jvm-stable","metaprogramming-lsi")
}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-04T13:35:05.376574
// Loaded: 1, Excluded: 0, Total: 1
include(":lib:apt-controller2feign-processor")
// <<< Gradle Buddy: End Of Block <<<

// Controller2Feign Processors
include(":lib:apt-controller2feign-processor")
include(":lib:ksp:metadata:controller2feign-processor")
