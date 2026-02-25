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

    val allRepos = gitDependencies.allResolved()
    if (allRepos.isNotEmpty()) {
        gitRepositories {
            allRepos.forEach { includeGitProject(scriptSettings, it) }
        }
    }
}
