pluginManagement {
    includeBuild("../../")
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

includeBuild("../../") {
    dependencySubstitution {
        substitute(module("site.addzero:kcp-all-object-jvm-static-plugin"))
            .using(project(":lib:kcp:all-object-jvm-static:kcp-all-object-jvm-static-plugin"))
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
