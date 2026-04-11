pluginManagement {
    val fallbackRepoVersion = "2026.04.12"
    val useIncludedBuild =
        System.getenv("ADDZERO_USE_INCLUDED_BUILD")
            ?.toBooleanStrictOrNull()
            ?: true

    fun readRepoVersion(): String {
        val gradlePropertiesFile = file("../../gradle.properties")
        if (!gradlePropertiesFile.isFile) {
            return fallbackRepoVersion
        }
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
            id("site.addzero.kcp.i18n") version pluginVersion
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
            substitute(module("site.addzero:kcp-i18n"))
                .using(project(":lib:kcp:kcp-i18n"))
            substitute(module("site.addzero:kcp-i18n-runtime"))
                .using(project(":lib:kcp:kcp-i18n-runtime"))
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
            from(files("../../checkouts/build-logic/gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "example-kcp-i18n"
