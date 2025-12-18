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
// Generated at: 2025-12-18T18:47:15.977543
// Loaded: 7, Excluded: 0, Total: 7
include(":lib:apt")
include(":lib:apt:apt-dict-processor")
include(":lib:apt:apt-ioc-processor")
include(":lib:gradle-plugin:project-plugin:gradle-apt-buddy")
include(":lib:ksp:metadata:ioc:ioc-core")
include(":lib:ksp:metadata:ioc:ioc-processor")
include(":lib:tool-jvm:database:tool-database-model")
// <<< Gradle Buddy: End Of Block <<<
