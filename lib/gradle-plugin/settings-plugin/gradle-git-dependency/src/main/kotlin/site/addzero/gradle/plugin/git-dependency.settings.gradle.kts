package site.addzero.gradle.plugin

import me.champeau.gradle.igp.GitIncludeExtension
import me.champeau.gradle.igp.gitRepositories

plugins {
    id("me.champeau.includegit")
}

val gitDependencys = extensions.create<GitDependencysExtension>("implementationRemoteGit")
val enableZlibs = gitDependencys.enableZlibs.get()
val zlibsName = gitDependencys.zlibsName.get()
val buidlogicName = gitDependencys.buildLogicName.get()
val remoteGits = gitDependencys.remoteGits.get()


val repoType = gitDependencys.repoType.get()
val auth = gitDependencys.auther.get()
val branchName = gitDependencys.branch.get()

if (enableZlibs) {
   includeBuild("checkouts/$buidlogicName")
}
fun GitIncludeExtension.includeGitProject(
    repoName: String,
) {
    include(repoName) {
        uri.set(repoType.urlTemplate.format(auth, repoName))
        branch.set(branchName)
    }
}
gitRepositories {
    if (remoteGits.isNotEmpty()) {
        remoteGits.forEach {
            includeGitProject(it)
        }
    }

    if (enableZlibs) {
        includeGitProject(buidlogicName)
    }


}

gradle.settingsEvaluated {
    if (enableZlibs) {
        dependencyResolutionManagement {
            versionCatalogs {
                create(zlibsName) {
                    from(files("./checkouts/$buidlogicName/gradle/libs.versions.toml"))
                }
            }
        }
    }
}
