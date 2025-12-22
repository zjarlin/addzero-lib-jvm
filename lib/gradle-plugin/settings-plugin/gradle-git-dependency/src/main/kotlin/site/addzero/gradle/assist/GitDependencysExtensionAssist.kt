package site.addzero.gradle.assist

import me.champeau.gradle.igp.GitIncludeExtension
import site.addzero.gradle.GitDependencysExtension
import java.io.File

/**
 *
 * @author zjarlin
 */
//    https://melix.github.io/includegit-gradle-plugin/latest/index.html#_known_limitations
fun GitIncludeExtension.includeGitProject(repoName: String, gitDependencysExtension: GitDependencysExtension) {
    val repoType = gitDependencysExtension.repoType.get()
    val author = gitDependencysExtension.author.get()
    val branchName = gitDependencysExtension.branch.get()
    val checkoutDir = gitDependencysExtension.checkoutDir.get()

    include(repoName) {
        uri.set(repoType.urlTemplate.format(author, repoName))
        branch.set(branchName)
        this.checkoutDirectory.set(File("$checkoutDir/$branchName"))
    }
}

