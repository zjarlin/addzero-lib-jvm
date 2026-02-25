package site.addzero.gradle

/**
 * Represents a single git repository dependency with its own configuration.
 * Unset fields will fall back to the global defaults in [GitDependencysExtension].
 */
data class GitRepoDependency(
    val name: String,
    var repoType: RepoType? = null,
    var author: String? = null,
    var branch: String? = null,
    var checkoutDir: String? = null,
)
