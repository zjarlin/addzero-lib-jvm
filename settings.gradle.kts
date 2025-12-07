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
// Generated at: 2025-12-07T13:51:19.465457
// Loaded: 4, Excluded: 0, Total: 4
include(":checkouts:metaprogramming-lsi:lsi-core")
include(":lib:tool-jvm:database:tool-ddlgenerator")
include(":lib:tool-jvm:network-call:tool-api-maven")
include(":lib:tool-kmp:tool-str")
// <<< Gradle Buddy: End Of Block <<<
