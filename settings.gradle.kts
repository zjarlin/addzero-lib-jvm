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
// Generated at: 2026-01-31T10:51:00.856890
// Loaded: 2, Excluded: 0, Total: 2
include(":lib:gradle-plugin:project-plugin:gradle-processor-buddy")
include(":lib:tool-jvm:tool-yml")
// <<< Gradle Module Sleep: End Of Block <<<
