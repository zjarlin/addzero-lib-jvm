rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")


//includeBuild("lib/gradle-plugin/addzero-gradle-repo-budy")


dependencyResolutionManagement {
    repositories {
//        mavenLocal()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}
pluginManagement {
    repositories {
//        mavenLocal()

//        applyGoogleRepository()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}


plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "+"
//    id("io.gitee.zjarlin.auto-modules") version "0.0.608"
    id("site.addzero.modules-buddy") version "+"
    id("me.champeau.includegit") version "0.3.2"
}

//includeBuild("build-logic")

gitRepositories {
    include("build-logic") {
        uri.set("https://gitee.com/zjarlin/build-logic.git")
        branch.set("master")
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./checkouts/build-logic/gradle/libs.versions.toml"))
        }
    }
}
includeBuild("checkouts/build-logic")
