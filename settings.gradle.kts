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
// Generated at: 2025-12-12T10:39:39.596286
// Loaded: 7, Excluded: 0, Total: 7
include(":lib:apt:apt-dict-annotations")
include(":lib:apt:apt-dict-processor")
include(":lib:apt:dict-trans:apt-dict-annotations")
include(":lib:apt:dict-trans:apt-dict-processor")
include(":lib:gradle-plugin:project-plugin:gradle-apt-buddy")
include(":lib:gradle-plugin:project-plugin:gradle-publish-budy")
include(":lib:tool-starter:dict-trans-spring-boot-starter")
// <<< Gradle Buddy: End Of Block <<<
