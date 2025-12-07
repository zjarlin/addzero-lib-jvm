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

//        ,"metaprogramming-lsi"

    )
}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-07T09:14:01.115375
// Loaded: 9, Excluded: 0, Total: 9
include(":lib:apt-controller2feign-processor")
include(":lib:apt-dict-processor")
include(":lib:gradle-plugin:project-plugin:gradle-publish-budy")
include(":lib:json2kotlin-dataclass")
include(":lib:tool-jvm:database:tool-database-model")
include(":lib:tool-jvm:database:tool-sql-executor")
include(":lib:tool-jvm:tool-jvmstr")
include(":lib:tool-jvm:tool-template-jte")
include(":lib:tool-starter:dict-trans-spring-boot-starter")
// <<< Gradle Buddy: End Of Block <<<

// Controller2Feign Processors
include(":lib:apt-controller2feign-processor")
include(":lib:ksp:metadata:controller2feign-processor")

// LSI APT
include(":checkouts:metaprogramming-lsi:lsi-core")
include(":checkouts:metaprogramming-lsi:lsi-apt")
