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

// 显式函数调用，在用户配置扩展后调用
fun Settings.includeRemoteGits(vararg repos: String) {
    gitRepositories {
        repos.forEach { includeGitProject(it) }
    }
}

// enableZlibs 的默认行为：包含 build-logic
gitRepositories {
    if (gitDependencies.enableZlibs.get()) {
        includeGitProject(gitDependencies.buildLogicName.get())
    }
}

gradle.settingsEvaluated {
    val enableZlibs = gitDependencies.enableZlibs.get()
    val zlibsName = gitDependencies.zlibsName.get()
    val buildLogicName = gitDependencies.buildLogicName.get()
    val remoteGits = gitDependencies.remoteGits.get()

    // 处理通过属性配置的 remoteGits（备选方案，推荐使用 includeRemoteGits 函数）
    if (remoteGits.isNotEmpty()) {
        gitRepositories {
            remoteGits.forEach { includeGitProject(it) }
        }
    }

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
