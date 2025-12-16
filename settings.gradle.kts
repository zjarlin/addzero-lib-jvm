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
// Generated at: 2025-12-16T09:01:03.903448
// Loaded: 4, Excluded: 0, Total: 4
include(":lib:gradle-plugin:project-plugin:conventions:java-convention")
include(":lib:gradle-plugin:project-plugin:conventions:kotlin-convention")
include(":lib:gradle-plugin:project-plugin:conventions:ktx-json-convention")
include(":lib:gradle-plugin:project-plugin:conventions:lombok-convention")
// <<< Gradle Buddy: End Of Block <<<
