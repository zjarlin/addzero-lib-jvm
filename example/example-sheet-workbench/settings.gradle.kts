pluginManagement {
    val useIncludedBuild =
        System.getenv("ADDZERO_USE_INCLUDED_BUILD")
            ?.toBooleanStrictOrNull()
            ?: true

    if (useIncludedBuild) {
        includeBuild("../../")
    }

    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

val useIncludedBuild =
    System.getenv("ADDZERO_USE_INCLUDED_BUILD")
        ?.toBooleanStrictOrNull()
        ?: true

if (useIncludedBuild) {
    includeBuild("../../") {
        dependencySubstitution {
            substitute(module("site.addzero:compose-sheet-spi"))
                .using(project(":lib:compose:compose-sheet-spi"))
            substitute(module("site.addzero:compose-native-component-sheet"))
                .using(project(":lib:compose:compose-native-component-sheet"))
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

rootProject.name = "example-sheet-workbench"
