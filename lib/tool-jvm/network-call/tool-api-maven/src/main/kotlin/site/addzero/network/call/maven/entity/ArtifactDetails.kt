package site.addzero.network.call.maven.entity

import java.time.LocalDateTime

/** 工件版本详情 */
data class ArtifactDetails(
    val groupId: String,
    val artifactId: String,
    val repositoryUrl: String,
    val latestVersion: String?,
    val releaseVersion: String?,
    val allVersions: List<String>,
    val lastUpdated: LocalDateTime?,
    val versionCount: Int
)

    /** 搜索API响应结构（内部使用） */
    internal data class SearchApiResponse(
        val code: String,
        val message: String,
        val data: SearchData
    )

    internal data class SearchData(
        val list: List<SearchItem>
    )

    internal data class SearchItem(
        val groupId: String,
        val artifactId: String,
        val version: String,
        val description: String?
    )
