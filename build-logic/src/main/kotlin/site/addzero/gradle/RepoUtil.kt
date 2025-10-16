package site.addzero.gradle

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.internal.serialize.codecs.core.NodeOwner
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.the


fun RepositoryHandler.applyGoogleRepository() {
    google {
        mavenContent {
            includeGroupAndSubgroups("androidx")
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
        }
    }
}


fun RepositoryHandler.applyCommonRepositories() {
    applyGoogleRepository()
    mavenCentral()
}

fun RepositoryHandler.applyPluginRepositories() {
    applyCommonRepositories()
    gradlePluginPortal()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
}
