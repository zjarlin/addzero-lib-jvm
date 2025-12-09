rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.git-dependency") version "+"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
implementationRemoteGit{
    remoteGits=listOf("addzero-lib-jvm-stable"
        ,"metaprogramming-lsi"
    )
}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-09T10:56:49.654257
// Loaded: 4, Excluded: 0, Total: 4
include(":lib:apt-dict-processor")
include(":lib:gradle-plugin:project-plugin:gradle-apt-buddy")
include(":lib:gradle-plugin:project-plugin:gradle-publish-budy")
include(":lib:tool-kmp:tool-str")
// <<< Gradle Buddy: End Of Block <<<
