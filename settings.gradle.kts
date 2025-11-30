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
// Generated at: 2025-11-30T17:25:13.746423
// Loaded: 5, Excluded: 1, Total: 6
// Excluded (build infrastructure): :checkouts:build-logic
include(":checkouts:addzero-lib-jvm-stable:lib:tool-jvm:tool-api-jvm")
include(":checkouts:compose-component:compose-component-shadcn")
include(":lib:ksp:common:ksp-easycode")
include(":lib:tool-jvm:jimmer:jimmer-ext-dynamic-datasource")
include(":lib:tool-jvm:tool-funbox")
// <<< Gradle Buddy: End Of Block <<<
