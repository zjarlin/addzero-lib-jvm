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
// Generated at: 2025-12-16T22:06:04.972297
// Loaded: 5, Excluded: 0, Total: 5
include(":lib:apt:dict-trans:apt-dict-trans-core")
include(":lib:decompile:struct-java")
include(":lib:kcp:kcp-annotations")
include(":lib:kcp:kcp-plugin")
include(":lib:tool-jvm:database:tool-sql-executor")
// <<< Gradle Buddy: End Of Block <<<
