rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.git-dependency") version "+"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
implementationRemoteGit{
    remoteGits=listOf("addzero-lib-jvm-stable","metaprogramming-lsi")
}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-05T09:52:46.188187
// Loaded: 9, Excluded: 0, Total: 9
include(":checkouts:metaprogramming-lsi:lsi-apt")
include(":checkouts:metaprogramming-lsi:lsi-core")
include(":checkouts:metaprogramming-lsi:lsi-kt")
include(":checkouts:metaprogramming-lsi:lsi-psi")
include(":checkouts:metaprogramming-lsi:lsi-reflection")
include(":lib:apt-controller2feign-processor")
include(":lib:apt-dict-processor")
include(":lib:gradle-plugin:project-plugin:gradle-publish-budy")
include(":lib:tool-jvm:tool-jvmstr")
// <<< Gradle Buddy: End Of Block <<<

// Controller2Feign Processors
include(":lib:apt-controller2feign-processor")
include(":lib:ksp:metadata:controller2feign-processor")

// LSI APT
include(":checkouts:metaprogramming-lsi:lsi-core")
include(":checkouts:metaprogramming-lsi:lsi-apt")
