pluginManagement {
    val fallbackRepoVersion = "2026.04.12"
    val useIncludedBuild =
        System.getenv("ADDZERO_USE_INCLUDED_BUILD")
            ?.toBooleanStrictOrNull()
            ?: true
    fun readRepoVersion(): String {
        val gradlePropertiesFile = file("../../gradle.properties")
        if (!gradlePropertiesFile.isFile) return fallbackRepoVersion
        return gradlePropertiesFile
            .readLines()
            .firstOrNull { line -> line.startsWith("version=") }
            ?.substringAfter("=")
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: fallbackRepoVersion
    }
    if (useIncludedBuild) {
        includeBuild("../../")
    }
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    if (!useIncludedBuild) {
        val pluginVersion = readRepoVersion()
        plugins {
            id("site.addzero.kcp.all-object-jvm-static") version pluginVersion
        }
    }
}

val useIncludedBuild =
    System.getenv("ADDZERO_USE_INCLUDED_BUILD")
        ?.toBooleanStrictOrNull()
        ?: true

if (useIncludedBuild) {
    includeBuild("../../") {
        dependencySubstitution {
            substitute(module("site.addzero:kcp-all-object-jvm-static-plugin"))
                .using(project(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-plugin"))
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
    versionCatalogs {
        create("libs") {
            from(files("../../build-logic/gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "example-all-object-jvm-static"

include(":kotlin-lib")
include(":java-app")
