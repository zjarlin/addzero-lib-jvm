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
// Generated at: 2026-01-31T10:29:27.986344
// Loaded: 17, Excluded: 0, Total: 17
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
include(":lib:gradle-plugin:project-plugin:gradle-processor-buddy")
include(":lib:tool-jvm:database:ddlgenerator")
include(":lib:tool-jvm:tool-yml")
// <<< Gradle Module Sleep: End Of Block <<<
