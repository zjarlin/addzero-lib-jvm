package site.addzero.gradle.plugin

import org.gradle.api.provider.Property

abstract class GitDependencysExtension {
    abstract val enableZlibs: Property<Boolean>
    abstract val zlibsName: Property<String>
    abstract val buildLogicName: Property<String>
    abstract val defaultRepoType: Property<RepoType>
    abstract val defaultOwner: Property<String>
    abstract val defaultBranch: Property<String>

    init {
        enableZlibs.convention(true)
        defaultRepoType.convention(RepoType.GITEE)
        zlibsName.convention("zlibs")
        buildLogicName.convention("build-logic")
        defaultOwner.convention("zjarlin")
        defaultBranch.convention("master")
    }
}
