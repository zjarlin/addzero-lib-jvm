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
// Generated at: 2025-12-28T11:29:58.302805
// Loaded: 6, Excluded: 0, Total: 6
include(":lib:apt:apt-controller2feign-processor")
include(":lib:gradle-plugin:project-plugin:conventions:jvm-conventions:koin-convention")
include(":lib:kcp:multireceiver:kcp-multireceiver-plugin")
include(":lib:ksp:metadata:ioc:ioc-processor")
include(":lib:tool-jvm:tool-spctx")
include(":lib:tool-kmp:tool-koin")
// <<< Gradle Buddy: End Of Block <<<
