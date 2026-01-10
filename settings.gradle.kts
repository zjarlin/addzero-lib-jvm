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
  id("site.addzero.gradle.plugin.modules-buddy") version "2026.01.11"
}
includeBuild("checkouts/build-logic")
implementationRemoteGit {
//  remoteGits = listOf(
//    "lsi"
////       ,"ddlgenerator"
//  )
}
// >>> Gradle Buddy: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-01-10T11:56:45.896189
// Loaded: 14, Excluded: 0, Total: 14
include(":lib:apt:apt-controller2feign-processor")
include(":lib:gradle-plugin:settings-plugin:gradle-modules-buddy")
include(":lib:lsi:lsi-apt")
include(":lib:tool-jvm:database:tool-sql-injection")
include(":lib:tool-jvm:models:common:common-models")
include(":lib:tool-jvm:tool-bean")
include(":lib:tool-jvm:tool-bytebuddy")
include(":lib:tool-jvm:tool-jsr")
include(":lib:tool-jvm:tool-reflection")
include(":lib:tool-jvm:tool-spring")
include(":lib:tool-kmp:tool-str")
include(":lib:tool-starter:controller-autoconfigure")
include(":lib:tool-starter:curllog-spring-boot-starter")
include(":lib:tool-starter:dict-trans-spring-boot-starter")
// <<< Gradle Buddy: End Of Block <<<
include(":lib:tool-jvm:tool-bean")
include(":lib:ksp:metadata:gen-reified:gen-reified-core")
