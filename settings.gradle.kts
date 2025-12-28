// 配置插件解析以使用 Maven 本地仓库
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
//    https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}
includeBuild("checkouts/build-logic")
implementationRemoteGit{
   remoteGits=listOf("lsi")
}

// kcp-reified modules
include(":lib:kcp:kcp-reified:kcp-reified-annotations")
include(":lib:kcp:kcp-reified:kcp-reified-plugin")
include(":lib:kcp:kcp-reified:kcp-reified-gradle")
include(":lib:kcp:kcp-reified:kcp-reified-test")
include(":lib:kcp:multireceiver:kcp-multireceiver-annotations")
include(":lib:kcp:multireceiver:kcp-multireceiver-plugin")

// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-28T21:28:47.860680
// Loaded: 3, Excluded: 0, Total: 3
include(":lib:tool-jvm:tool-minio")
include(":lib:tool-kmp:tool-koin")
include(":lib:tool-kmp:tool-str")
// <<< Gradle Buddy: End Of Block <<<
