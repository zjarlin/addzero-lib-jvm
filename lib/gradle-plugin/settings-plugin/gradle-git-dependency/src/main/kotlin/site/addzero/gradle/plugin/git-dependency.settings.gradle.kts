package site.addzero.gradle.plugin

import me.champeau.gradle.igp.gitRepositories
import site.addzero.gradle.GitDependencysExtension
import site.addzero.gradle.assist.includeGitProject

plugins {
    id("me.champeau.includegit")
}

val gitDependencies = extensions.create<GitDependencysExtension>("implementationRemoteGit")

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
