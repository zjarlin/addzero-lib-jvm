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
// Generated at: 2025-12-04T16:09:59.387142
// Loaded: 8, Excluded: 0, Total: 8
include(":checkouts:metaprogramming-lsi:lsi-apt")
include(":checkouts:metaprogramming-lsi:lsi-core")
include(":checkouts:metaprogramming-lsi:lsi-intellij")
include(":checkouts:metaprogramming-lsi:lsi-kt")
include(":checkouts:metaprogramming-lsi:lsi-psi")
include(":checkouts:metaprogramming-lsi:lsi-psiandkt")
include(":checkouts:metaprogramming-lsi:lsi-reflection")
include(":lib:apt-controller2feign-processor")
// <<< Gradle Buddy: End Of Block <<<

// Controller2Feign Processors
include(":lib:apt-controller2feign-processor")
include(":lib:ksp:metadata:controller2feign-processor")

// LSI APT
include(":checkouts:metaprogramming-lsi:lsi-core")
include(":checkouts:metaprogramming-lsi:lsi-apt")
