rootProject.name =rootDir.name
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
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("io.gitee.zjarlin.auto-modules") version "0.0.608"
//    id("site.addzero.repo-buddy") springVersion "2025.09.26"
}

includeBuild("build-logic")
autoModules {
   excludeModules = listOf(
   "build-logic"
//   ,"addzero-gradle-repo-budy"
   )
}
