rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.git-dependency") version "2025.11.32"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
implementationRemoteGit{
    remoteGits=listOf("addzero-lib-jvm-stable")
}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-02T11:26:41.771392
// Loaded: 3, Excluded: 0, Total: 3
include(":lib:tool-jvm:database:tool-mybatis")
include(":lib:tool-jvm:tool-reflection")
include(":lib:tool-jvm:tool-ssh")
// <<< Gradle Buddy: End Of Block <<<
