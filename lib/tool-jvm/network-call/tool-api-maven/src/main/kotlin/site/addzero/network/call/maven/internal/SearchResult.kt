package site.addzero.network.call.maven.internal

/** 搜索结果 */
data class SearchResult(
    val groupId: String,
    val artifactId: String,
    val latestVersion: String,
    val description: String,
    val repository: String
)
