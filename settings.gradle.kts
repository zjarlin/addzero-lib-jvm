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
// Generated at: 2025-12-08T18:05:56.783746
// Loaded: 14, Excluded: 0, Total: 14
include(":checkouts:metaprogramming-lsi:lsi-apt")
include(":checkouts:metaprogramming-lsi:lsi-core")
include(":checkouts:metaprogramming-lsi:lsi-database")
include(":checkouts:metaprogramming-lsi:lsi-intellij")
include(":checkouts:metaprogramming-lsi:lsi-kt")
include(":checkouts:metaprogramming-lsi:lsi-psi")
include(":checkouts:metaprogramming-lsi:lsi-psiandkt")
include(":checkouts:metaprogramming-lsi:lsi-reflection")
include(":lib:apt-controller2feign-processor")
include(":lib:json2kotlin-dataclass")
include(":lib:ksp:common:ksp-support-jdbc")
include(":lib:tool-jvm:database:tool-database-model")
include(":lib:tool-jvm:database:tool-sql-executor")
include(":lib:tool-starter:dict-trans-spring-boot-starter")
// <<< Gradle Buddy: End Of Block <<<
