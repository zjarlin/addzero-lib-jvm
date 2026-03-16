pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    google()
  }
  plugins {
    id("site.addzero.kcp.multireceiver") version "2026.03.13"
    id("site.addzero.kcp.transform-overload") version "2026.03.13"
  }
}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
  id("site.addzero.gradle.plugin.repo-buddy") version "+"
//  id("site.addzero.gradle.plugin.modules-buddy") version "2026.01.11"
}
includeBuild("build-logic")


// >>> Gradle Module Sleep: On-Demand Modules (DO NOT EDIT THIS BLOCK) >>>
// Generated at: 2026-03-16T17:29:50.190522
// Loaded: 5, Excluded: 0, Total: 5
include(":lib:biz:spec-iot")
include(":lib:gradle-plugin:conventions:jvm-conventions:koin-convention")
include(":lib:ksp:metadata:spring2ktor-server-core")
include(":lib:ksp:metadata:spring2ktor-server-processor")
include(":lib:ksp:metadata:spring2ktor-server-smoke")
// <<< Gradle Module Sleep: End Of Block <<<

include(":lib:ksp:metadata:spring2ktor-server-core")
include(":lib:ksp:metadata:spring2ktor-server-processor")
include(":lib:ksp:metadata:spring2ktor-server-smoke")
include(":lib:tool-kmp:jdbc:tool-jdbc-model")
include(":lib:tool-kmp:jdbc:tool-jdbc")
include(":lib:tool-jvm:database:tool-database-model")
include(":lib:tool-jvm:database:ddlgenerator-core")
include(":lib:tool-jvm:database:ddlgenerator-lsi-adaptor")
include(":lib:tool-jvm:database:ddlgenerator-jdbc-adaptor")
include(":lib:tool-jvm:database:ddlgenerator-dialect-mysql")
include(":lib:tool-jvm:database:ddlgenerator-dialect-postgresql")
include(":lib:tool-jvm:database:ddlgenerator-dialect-h2")
include(":lib:tool-jvm:database:ddlgenerator-dialect-sqlite")
include(":lib:tool-jvm:database:ddlgenerator-dialect-sqlserver")
include(":lib:tool-jvm:database:ddlgenerator-dialect-oracle")
include(":lib:tool-jvm:database:ddlgenerator-dialect-dm")
include(":lib:tool-jvm:database:ddlgenerator-dialect-kingbase")
include(":lib:tool-jvm:database:ddlgenerator-dialect-taos")
include(":lib:tool-jvm:database:ddlgenerator")
include(":lib:biz:spec-iot")
