package site.addzero.gradle.plugin

import me.champeau.gradle.igp.gitRepositories
import site.addzero.gradle.GitDependencysExtension
import site.addzero.gradle.GitRepoDependency

plugins {
    id("site.addzero.gradle.plugin.git-dependency")
}

val gitDependencies = the<GitDependencysExtension>()
gradle.settingsEvaluated {
    val scriptSettings: Settings = this
    val enableZlibs = gitDependencies.enableZlibs.get()
    val zlibsName = gitDependencies.zlibsName.get()
    val buildLogicName = gitDependencies.buildLogicName.get()

    if (enableZlibs) {
        val resolved = gitDependencies.resolve(GitRepoDependency(buildLogicName))
        gitRepositories {
            includeGitProject(scriptSettings, resolved)
        }
        includeBuild("${resolved.checkoutDir}/$buildLogicName")
        dependencyResolutionManagement {
            versionCatalogs {
                create(zlibsName) {
                    from(files("./${resolved.checkoutDir}/$buildLogicName/gradle/libs.versions.toml"))
                }
            }
        }
    }
}
