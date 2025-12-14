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
    remoteGits=listOf("addzero-lib-jvm-stable"
        ,"metaprogramming-lsi"
    )
}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-13T18:12:29.574692
// Loaded: 5, Excluded: 0, Total: 5
include(":lib:apt:dict-trans:apt-dict-annotations")
include(":lib:apt:dict-trans:apt-dict-processor")
include(":lib:tool-jvm:tool-ai")
include(":lib:tool-starter:dict-trans-core")
include(":lib:tool-starter:dict-trans-spring-boot-starter")
// <<< Gradle Buddy: End Of Block <<<
