package site.addzero.gradle.plugin

import me.champeau.gradle.igp.GitIncludeExtension
import me.champeau.gradle.igp.gitRepositories

plugins {
    id("me.champeau.includegit")
}

val gitDependencies = extensions.create<GitDependencysExtension>("implementationRemoteGit")

fun GitIncludeExtension.includeGitProject(repoName: String) {
    val repoType = gitDependencies.repoType.get()
    val author = gitDependencies.author.get()
    val branchName = gitDependencies.branch.get()
    include(repoName) {
        uri.set(repoType.urlTemplate.format(author, repoName))
        branch.set(branchName)
    }
}

gitRepositories {
    val enableZlibs = gitDependencies.enableZlibs.get()
    val buildLogicName = gitDependencies.buildLogicName.get()
    val remoteGits = gitDependencies.remoteGits.get()
    
    if (enableZlibs) {
        includeGitProject(buildLogicName)
    }
    if (remoteGits.isNotEmpty()) {
        remoteGits.forEach { includeGitProject(it) }
    }
}

gradle.settingsEvaluated {
    val enableZlibs = gitDependencies.enableZlibs.get()
    val zlibsName = gitDependencies.zlibsName.get()
    val buildLogicName = gitDependencies.buildLogicName.get()
    
    if (enableZlibs) {
        includeBuild("checkouts/$buildLogicName")
        dependencyResolutionManagement {
            versionCatalogs {
                create(zlibsName) {
                    from(files("./checkouts/$buildLogicName/gradle/libs.versions.toml"))
                }
            }
        }
    }
}
