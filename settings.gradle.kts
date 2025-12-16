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
// Generated at: 2025-12-16T13:52:12.637325
// Loaded: 5, Excluded: 0, Total: 5
include(":lib:gradle-plugin:project-plugin:conventions:jvm-conventions:intellij-convention")
include(":lib:gradle-plugin:project-plugin:conventions:jvm-conventions:koin-convention")
include(":lib:gradle-plugin:project-plugin:conventions:koin-convention")
include(":lib:gradle-plugin:project-plugin:conventions:ksp-convention")
include(":lib:gradle-plugin:settings-plugin:gradle-git-dependency")
// <<< Gradle Buddy: End Of Block <<<
