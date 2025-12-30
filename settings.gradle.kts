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
       ,"ddlgenerator"
   )
}
// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2025-12-30T16:27:17.370312
// Loaded: 3, Excluded: 0, Total: 3
include(":checkouts:ddlgenerator")
include(":lib:ksp:metadata:gen-reified:gen-reified-core")
include(":lib:ksp:metadata:gen-reified:gen-reified-processor")
// <<< Gradle Buddy: End Of Block <<<
