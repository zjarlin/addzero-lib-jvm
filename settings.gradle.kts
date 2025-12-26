rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
//    https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
//implementationRemoteGit{
//    remoteGits=listOf(
//        ""
//    )
//}

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-26T10:05:16.401653
// Loaded: 6, Excluded: 0, Total: 6
include(":lib:gradle-plugin:project-plugin:gradle-processor-buddy")
include(":lib:ksp:metadata:ioc:ioc-core")
include(":lib:ksp:metadata:ioc:ioc-processor")
include(":lib:tool-jvm:models:common:common-models")
include(":lib:tool-jvm:tool-common-jvm")
include(":lib:tool-jvm:tool-rustfs")
// <<< Gradle Buddy: End Of Block <<<
