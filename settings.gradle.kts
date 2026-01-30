rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "+"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
//    https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "2026.01.11"
}

// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-01-30T14:42:36.302491
// Loaded: 16, Excluded: 0, Total: 16
include(":lib:gradle-plugin:conventions:intellij-convention")
include(":lib:gradle-plugin:conventions:jvm-conventions:graalvm-convention")
include(":lib:gradle-plugin:conventions:jvm-conventions:java-convention")
include(":lib:gradle-plugin:conventions:jvm-conventions:koin-convention")
include(":lib:gradle-plugin:conventions:jvm-conventions:kotlin-convention")
include(":lib:gradle-plugin:conventions:jvm-conventions:ksp-convention")
include(":lib:gradle-plugin:conventions:jvm-conventions:ktxjson-convention")
include(":lib:gradle-plugin:conventions:jvm-conventions:lombok-convention")
include(":lib:gradle-plugin:conventions:jvm-conventions:spring-convention")
include(":lib:gradle-plugin:conventions:kmp-conventions:kmp-convention")
include(":lib:gradle-plugin:conventions:koin-convention")
include(":lib:gradle-plugin:conventions:ksp-conventions")
include(":lib:gradle-plugin:conventions:other-conventions")
include(":lib:gradle-plugin:conventions:spring-conventions")
include(":lib:gradle-plugin:settings-plugin:gradle-modules-buddy")
include(":lib:tool-kmp:tool-regex")
// <<< Gradle Module Sleep: End Of Block <<<
