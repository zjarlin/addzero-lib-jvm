rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
//    https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations
    id("site.addzero.gradle.plugin.git-dependency") version "+"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
implementationRemoteGit{
    remoteGits=listOf(

        "addzero-lib-jvm-stable"
        ,"metaprogramming-lsi"
    )
}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-16T10:14:58.308563
// Loaded: 3, Excluded: 0, Total: 3
include(":lib:gradle-plugin:project-plugin:conventions:ktxjson-convention")
include(":lib:gradle-plugin:project-plugin:conventions:lombok-convention")
include(":lib:tool-jvm:network-call:tool-api-maven")
// <<< Gradle Buddy: End Of Block <<<
