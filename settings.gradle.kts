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
// Generated at: 2025-12-19T11:44:20.818466
// Loaded: 6, Excluded: 0, Total: 6
include(":lib:apt")
include(":lib:apt:apt-ioc-processor")
include(":lib:tool-jvm:database:tool-database-model")
include(":lib:tool-jvm:database:tool-sql-executor")
include(":lib:tool-jvm:tool-yml")
include(":lib:tool-kmp:jdbc:tool-jdbc")
// <<< Gradle Buddy: End Of Block <<<
