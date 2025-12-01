rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.git-dependency") version "2025.11.32"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
implementationRemoteGit{
    remoteGits=listOf("addzero-lib-jvm-stable")
}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-01T20:30:59.679319
// Loaded: 3, Excluded: 0, Total: 3
include(":lib:gradle-plugin:project-plugin:gradle-version-budy")
include(":lib:gradle-plugin:settings-plugin:gradle-git-dependency")
include(":lib:json2kotlin-dataclass")
// <<< Gradle Buddy: End Of Block <<<
