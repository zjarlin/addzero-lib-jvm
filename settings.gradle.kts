rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
//  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-06-04T16:17:34.851396
// Loaded: 4, Excluded: 0, Total: 4
include(":lib:ksp:route:route-core")
include(":lib:ksp:route:route-gradle-plugin")
include(":lib:ksp:route:route-processor")
include(":lib:tool-kmp:models:common:common-models")
// <<< Gradle Module Sleep: End Of Block <<<
