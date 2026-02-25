package site.addzero.gradle.plugin

import me.champeau.gradle.igp.GitIncludeExtension
import org.gradle.api.initialization.Settings
import site.addzero.gradle.GitDependencysExtension
import site.addzero.gradle.ResolvedGitRepo
import java.io.File

/**
 * Include a git project using a fully resolved repo config.
 */
internal fun GitIncludeExtension.includeGitProject(
    settings: Settings,
    repo: ResolvedGitRepo,
) {
    include(repo.name) {
        uri.set(repo.uri)
        branch.set(repo.branch)
        val checkoutRoot = File(settings.settingsDir, repo.checkoutDir)
        checkoutDirectory.set(File(checkoutRoot, repo.name))
    }
}

/**
 * Legacy overload — delegates to the [ResolvedGitRepo] version using global defaults.
 */
internal fun GitIncludeExtension.includeGitProject(
    settings: Settings,
    repoName: String,
    gitDependencysExtension: GitDependencysExtension,
) {
    val resolved = ResolvedGitRepo(
        name = repoName,
        repoType = gitDependencysExtension.repoType.get(),
        author = gitDependencysExtension.author.get(),
        branch = gitDependencysExtension.branch.get(),
        checkoutDir = gitDependencysExtension.checkoutDir.get(),
    )
    includeGitProject(settings, resolved)
}
