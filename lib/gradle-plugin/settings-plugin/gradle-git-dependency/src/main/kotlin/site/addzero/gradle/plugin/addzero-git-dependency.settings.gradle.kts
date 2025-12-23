package site.addzero.gradle.plugin

import me.champeau.gradle.igp.GitIncludeExtension
import me.champeau.gradle.igp.gitRepositories
import site.addzero.gradle.GitDependencysExtension

plugins {
    id("site.addzero.gradle.plugin.git-dependency")
}
fun GitIncludeExtension.includeGitProject(repoName: String, gitDependencysExtension: GitDependencysExtension) {
    val repoType = gitDependencysExtension.repoType.get()
    val author = gitDependencysExtension.author.get()
    val branchName = gitDependencysExtension.branch.get()
    val checkoutDir = gitDependencysExtension.checkoutDir.get()

    include(repoName) {
        uri.set(repoType.urlTemplate.format(author, repoName))
        branch.set(branchName)
        this.checkoutDirectory.set(file("$checkoutDir/$repoName"))
    }
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
