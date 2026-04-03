pluginManagement {
    val useIncludedBuild =
        System.getenv("ADDZERO_USE_INCLUDED_BUILD")
            ?.toBooleanStrictOrNull()
            ?: true

    fun readRepoVersion(): String {
        val gradlePropertiesFile = file("../../gradle.properties")
        if (!gradlePropertiesFile.isFile) {
            return "2026.10330.12238"
        }
        return gradlePropertiesFile
            .readLines()
            .firstOrNull { line -> line.startsWith("version=") }
            ?.substringAfter("=")
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: "2026.10330.12238"
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
            id("site.addzero.kcp.spread-pack") version pluginVersion
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
            substitute(module("site.addzero:kcp-spread-pack-plugin"))
                .using(project(":lib:kcp:spread-pack:kcp-spread-pack-plugin"))
            substitute(module("site.addzero:kcp-spread-pack-annotations"))
                .using(project(":lib:kcp:spread-pack:kcp-spread-pack-annotations"))
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

rootProject.name = "example-spread-pack"
