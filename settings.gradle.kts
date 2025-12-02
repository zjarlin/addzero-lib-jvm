rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.git-dependency") version "+"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
implementationRemoteGit{
    remoteGits=listOf("addzero-lib-jvm-stable")
}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-02T22:25:43.992825
// Loaded: 3, Excluded: 0, Total: 3
include(":lib:tool-jvm:network-call:tool-api-maven")
include(":lib:tool-jvm:tool-jvmstr")
include(":lib:tool-kmp:tool-str")
// <<< Gradle Buddy: End Of Block <<<
