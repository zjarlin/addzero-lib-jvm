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
// Generated at: 2025-12-01T20:46:29.652853
// Loaded: 3, Excluded: 0, Total: 3
include(":lib:gradle-plugin:project-plugin:gradle-publish-budy")
include(":lib:gradle-plugin:settings-plugin:gradle-git-dependency")
include(":lib:json2kotlin-dataclass")
// <<< Gradle Buddy: End Of Block <<<
