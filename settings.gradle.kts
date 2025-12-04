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
// Generated at: 2025-12-04T10:35:16.770831
// Loaded: 7, Excluded: 0, Total: 7
include(":lib:tool-jvm:database:mybatis-auto-wrapper")
include(":lib:tool-jvm:database:mybatis-auto-wrapper-core")
include(":lib:tool-jvm:database:tool-mybatis")
include(":lib:tool-jvm:jimmer:jimmer-ext-dynamic-datasource")
include(":lib:tool-jvm:tool-reflection")
include(":lib:tool-jvm:tool-ssh")
include(":lib:tool-kmp:tool-str")
// <<< Gradle Buddy: End Of Block <<<
