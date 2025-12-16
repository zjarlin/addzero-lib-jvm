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
// Generated at: 2025-12-16T20:57:44.322138
// Loaded: 7, Excluded: 0, Total: 7
include(":checkouts:addzero-lib-jvm-stable:lib:tool-jvm:tool-api-jvm")
include(":lib:decompile:struct-java")
include(":lib:gradle-plugin:settings-plugin:gradle-git-dependency")
include(":lib:kcp:kcp-annotations")
include(":lib:kcp:kcp-i18n")
include(":lib:kcp:kcp-plugin")
include(":lib:tool-jvm:network-call:tool-api-ocr")
// <<< Gradle Buddy: End Of Block <<<
