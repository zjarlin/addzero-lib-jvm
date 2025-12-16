package site.addzero.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class GitDependencysExtension {
    abstract val remoteGits: ListProperty<String>
    abstract val enableZlibs: Property<Boolean>
    abstract val zlibsName: Property<String>
    abstract val buildLogicName: Property<String>
    abstract val repoType: Property<RepoType>
    abstract val author: Property<String>
    abstract val branch: Property<String>
    abstract val checkoutDir: Property<String>

    init {
        remoteGits.convention(listOf<String>())
        enableZlibs.convention(true)
        repoType.convention(RepoType.GITEE)
        zlibsName.convention("libs")
        buildLogicName.convention("build-logic")
        author.convention("zjarlin")
        branch.convention("master")
//        checkouts
        checkoutDir.convention("lib-git")
    }
}
