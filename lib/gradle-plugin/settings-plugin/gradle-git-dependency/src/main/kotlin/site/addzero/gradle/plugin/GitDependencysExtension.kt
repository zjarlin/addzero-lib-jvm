package site.addzero.gradle.plugin

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class GitDependencysExtension {
    abstract val remoteGits: ListProperty<String>
    abstract val enableZlibs: Property<Boolean>
    abstract val zlibsName: Property<String>
    abstract val buildLogicName: Property<String>
    abstract val repoType: Property<RepoType>
    abstract val auther: Property<String>
    abstract val branch: Property<String>

    init {
        remoteGits.convention(listOf<String>())
        enableZlibs.convention(true)
        repoType.convention(RepoType.GITEE)
        zlibsName.convention("libs")
        buildLogicName.convention("build-logic")
        auther.convention("zjarlin")
        branch.convention("master")
    }
}
