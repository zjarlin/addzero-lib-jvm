package site.addzero.gradle.plugin

import me.champeau.gradle.igp.gitRepositories
import org.gradle.api.initialization.Settings
import site.addzero.gradle.GitDependencysExtension

plugins {
    id("me.champeau.includegit")
}

val gitDependencies = extensions.create<GitDependencysExtension>("implementationRemoteGit")


gradle.settingsEvaluated {
    val scriptSettings: Settings = this


    // 显式函数调用，在用户配置扩展后调用
    fun Settings.includeRemoteGits(vararg repos: String) {
        val targetSettings = this
        gitRepositories {
            repos.forEach { includeGitProject(targetSettings, it, gitDependencies) }
        }
    }

    val remoteGits = gitDependencies.remoteGits.get()

    if (remoteGits.isNotEmpty()) {
        gitRepositories {
            remoteGits.forEach { includeGitProject(scriptSettings, it, gitDependencies) }
        }
    }
}
