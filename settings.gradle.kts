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
   remoteGits=listOf("lsi"
//       ,"ddlgenerator"
   )
}
// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-01-08T11:25:31.833218
// Loaded: 1, Excluded: 0, Total: 1
include(":lib:tool-jvm:tool-handwriting")
// <<< Gradle Buddy: End Of Block <<<
include(":lib:ksp:metadata:gen-reified:gen-reified-core")
