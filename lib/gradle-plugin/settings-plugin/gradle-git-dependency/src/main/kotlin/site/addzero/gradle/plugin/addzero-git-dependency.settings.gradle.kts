package site.addzero.gradle.plugin

import me.champeau.gradle.igp.gitRepositories
import site.addzero.gradle.GitDependencysExtension
import site.addzero.gradle.assist.includeGitProject

plugins {
    id("site.addzero.gradle.plugin.git-dependency")
}

val gitDependencies = the<GitDependencysExtension>()
gradle.settingsEvaluated {
    val enableZlibs = gitDependencies.enableZlibs.get()
    val zlibsName =gitDependencies.zlibsName.get()
    val buildLogicName = gitDependencies.buildLogicName.get()
    val checkoutDir = gitDependencies.checkoutDir.get()


    gitRepositories {
        if (enableZlibs) {
            includeGitProject(buildLogicName, gitDependencies)
        }
    }
    if (enableZlibs) {
        includeBuild("$checkoutDir/$buildLogicName")
        dependencyResolutionManagement {
            versionCatalogs {
                create(zlibsName) {
                    from(files("./$checkoutDir/$buildLogicName/gradle/libs.versions.toml"))
                }
            }
        }

    }


}
