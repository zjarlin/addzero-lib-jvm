package site.addzero.gradle.plugin

import me.champeau.gradle.igp.GitIncludeExtension

plugins {
    id("me.champeau.includegit")
}

val gitDependencys = extensions.create<GitDependencysExtension>("gitDependencys")
val enableZlibs = gitDependencys.enableZlibs.get()
val zlibsName = gitDependencys.zlibsName.get()
val buidlogicName = gitDependencys.buildLogicName.get()

fun GitIncludeExtension.include(
    projectName: String,
    repoType: RepoType = gitDependencys.defaultRepoType.get(),
    owner: String = gitDependencys.defaultOwner.get(),
    branchName: String = gitDependencys.defaultBranch.get()
) {
    include(projectName) {
        uri.set(repoType.urlTemplate.format(owner, projectName))
        branch.set(branchName)
    }
}

fun GitIncludeExtension.include(vararg projectNames: String) {
    if (enableZlibs) {
        include(buidlogicName)
    }
    projectNames.forEach {
        include(
            it,
            gitDependencys.defaultRepoType.get(),
            gitDependencys.defaultOwner.get(),
            gitDependencys.defaultBranch.get()
        )
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
