package site.addzero.gradle.plugin

import me.champeau.gradle.igp.GitIncludeExtension
import org.gradle.api.initialization.Settings
import site.addzero.gradle.GitDependencysExtension
import java.io.File

internal fun GitIncludeExtension.includeGitProject(
    settings: Settings,
    repoName: String,
    gitDependencysExtension: GitDependencysExtension,
) {
    val repoType = gitDependencysExtension.repoType.get()
    val author = gitDependencysExtension.author.get()
    val branchName = gitDependencysExtension.branch.get()
    val checkoutDir = gitDependencysExtension.checkoutDir.get()

    include(repoName) {
        uri.set(repoType.urlTemplate.format(author, repoName))
        branch.set(branchName)
        val checkoutRoot = File(settings.settingsDir, checkoutDir)
        checkoutDirectory.set(File(checkoutRoot, repoName))
    }
}
