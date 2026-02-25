package site.addzero.gradle

import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class GitDependencysExtension {
    // ── Global defaults ──
    abstract val repoType: Property<RepoType>
    abstract val author: Property<String>
    abstract val branch: Property<String>
    abstract val checkoutDir: Property<String>

    // ── Per-repo list (new DSL) ──
    val repos: MutableList<GitRepoDependency> = mutableListOf()

    // ── Legacy list (backward compat) ──
    abstract val remoteGits: ListProperty<String>

    //    addzero用到
    abstract val enableZlibs: Property<Boolean>
    abstract val zlibsName: Property<String>
    abstract val buildLogicName: Property<String>
//    addzero用到end

    init {
        remoteGits.convention(listOf<String>())
        repoType.convention(RepoType.GITEE)
        author.convention("zjarlin")
        branch.convention("master")
        checkoutDir.convention("checkouts")

//    addzero用到
        enableZlibs.convention(true)
        zlibsName.convention("libs")
        buildLogicName.convention("build-logic")
//    addzero用到end
    }

    /**
     * Add a repo using global defaults.
     * ```
     * repo("my-lib")
     * ```
     */
    fun repo(name: String) {
        repos.add(GitRepoDependency(name))
    }

    /**
     * Add a repo with per-repo overrides.
     * ```
     * repo("my-lib") {
     *     repoType = RepoType.GITHUB
     *     author = "other-user"
     *     branch = "main"
     * }
     * ```
     */
    fun repo(name: String, action: Action<GitRepoDependency>) {
        val dep = GitRepoDependency(name)
        action.execute(dep)
        repos.add(dep)
    }

    /**
     * Resolve a [GitRepoDependency] by merging per-repo values with global defaults.
     */
    fun resolve(dep: GitRepoDependency): ResolvedGitRepo {
        return ResolvedGitRepo(
            name = dep.name,
            repoType = dep.repoType ?: repoType.get(),
            author = dep.author ?: author.get(),
            branch = dep.branch ?: branch.get(),
            checkoutDir = dep.checkoutDir ?: checkoutDir.get(),
        )
    }

    /**
     * All repos: new DSL repos + legacy [remoteGits] (converted with global defaults).
     */
    fun allResolved(): List<ResolvedGitRepo> {
        val fromDsl = repos.map { resolve(it) }
        val fromLegacy = remoteGits.get().map { name ->
            ResolvedGitRepo(
                name = name,
                repoType = repoType.get(),
                author = author.get(),
                branch = branch.get(),
                checkoutDir = checkoutDir.get(),
            )
        }
        return fromDsl + fromLegacy
    }
}

/**
 * Fully resolved git repo config — no nulls, ready to use.
 */
data class ResolvedGitRepo(
    val name: String,
    val repoType: RepoType,
    val author: String,
    val branch: String,
    val checkoutDir: String,
) {
    val uri: String get() = repoType.urlTemplate.format(author, name)
}
