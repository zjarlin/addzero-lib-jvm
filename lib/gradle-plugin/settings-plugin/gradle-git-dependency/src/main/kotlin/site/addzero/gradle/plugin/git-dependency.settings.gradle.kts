package site.addzero.gradle.plugin

import me.champeau.gradle.igp.GitIncludeExtension
import me.champeau.gradle.igp.gitRepositories
import site.addzero.gradle.GitDependencysExtension

plugins {
    id("me.champeau.includegit")
}

val gitDependencies = extensions.create<GitDependencysExtension>("implementationRemoteGit")

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


gradle.settingsEvaluated {


    // 显式函数调用，在用户配置扩展后调用
    fun Settings.includeRemoteGits(vararg repos: String) {
        gitRepositories {
            repos.forEach { includeGitProject(it, gitDependencies) }
        }
    }

    val remoteGits = gitDependencies.remoteGits.get()

    if (remoteGits.isNotEmpty()) {
        gitRepositories {
            remoteGits.forEach { includeGitProject(it, gitDependencies) }
        }
    }
}
