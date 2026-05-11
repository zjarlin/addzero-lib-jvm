rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
//  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
  id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "+"
}



// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-04-19T10:57:10.406542
// Loaded: 24, Excluded: 0, Total: 24
include(":lib:gradle-plugin:project-plugin:gradle-processor-buddy")
include(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi-helper")
include(":lib:ksp:metadata:modbus:modbus-codegen-core")
include(":lib:tool-jvm:network-call:browser:tool-api-browser-automation")
include(":lib:tool-jvm:network-call:browser:ws-automation")
include(":lib:tool-jvm:network-call:chat:api-models-chat")
include(":lib:tool-jvm:network-call:music:api-music-spi")
include(":lib:tool-jvm:network-call:music:api-netease")
include(":lib:tool-jvm:network-call:music:api-netease-semantic-impl")
include(":lib:tool-jvm:network-call:music:tool-api-music-search")
include(":lib:tool-jvm:network-call:music:tool-api-suno")
include(":lib:tool-jvm:network-call:music:tool-music-design")
include(":lib:tool-jvm:network-call:tool-api-maven")
include(":lib:tool-jvm:network-call:tool-api-ocr")
include(":lib:tool-jvm:network-call:tool-api-payment")
include(":lib:tool-jvm:network-call:tool-api-soft-download")
include(":lib:tool-jvm:network-call:tool-api-temp-mail")
include(":lib:tool-jvm:network-call:tool-api-translate")
include(":lib:tool-jvm:network-call:tool-api-tyc")
include(":lib:tool-jvm:network-call:tool-api-tyc-hw")
include(":lib:tool-jvm:network-call:tool-api-video-parse")
include(":lib:tool-jvm:network-call:tool-api-video-search-and-download")
include(":lib:tool-jvm:network-call:tool-api-weather")
include(":lib:tool-kmp:network-starter")
// <<< Gradle Module Sleep: End Of Block <<<
