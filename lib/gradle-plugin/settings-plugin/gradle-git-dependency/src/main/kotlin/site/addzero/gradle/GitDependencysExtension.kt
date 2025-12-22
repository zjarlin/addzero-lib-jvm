package site.addzero.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class GitDependencysExtension {
    abstract val remoteGits: ListProperty<String>
    abstract val repoType: Property<RepoType>
    abstract val author: Property<String>
    abstract val branch: Property<String>
    abstract val checkoutDir: Property<String>


    //    addzero用到
    abstract val enableZlibs: Property<Boolean>
    abstract val zlibsName: Property<String>
    abstract val buildLogicName: Property<String>
//    addzero用到end


    init {
        remoteGits.convention(listOf<String>())
        repoType.convention(RepoType.GITHUB)
        author.convention("zjarlin")
        branch.convention("main")
//        checkouts
        checkoutDir.convention("checkouts")

//    addzero用到
        enableZlibs.convention(true)
        zlibsName.convention("libs")
        buildLogicName.convention("build-logic")
//    addzero用到end
    }
}
