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
// Generated at: 2025-11-30T17:36:37.625480
// Only these modules will be loaded:
include(":checkouts:build-logic")
include(":checkouts:compose-component:compose-component-shadcn")
include(":lib:gradle-plugin:project-plugin:gradle-publish-budy")
include(":lib:gradle-plugin:settings-plugin:gradle-modules-buddy")
include(":lib:tool-jvm:jimmer:jimmer-ext-lowquery")
// <<< Gradle Buddy: End Of Block <<<
