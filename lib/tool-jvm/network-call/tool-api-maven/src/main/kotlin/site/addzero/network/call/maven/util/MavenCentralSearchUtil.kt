package site.addzero.network.call.maven.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 基于Maven Central REST API的搜索工具类
 * 参考: https://central.sonatype.org/search/rest-api-guide/
 */
object MavenCentralSearchUtil {

    // Maven Central搜索API基础URL (已更新为新端点)
    private const val SEARCH_API_BASE = "https://central.sonatype.com/solrsearch/select"

    // 初始化OkHttp客户端
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // 初始化Jackson ObjectMapper
    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * 通过groupId搜索该组下的工件，并返回最新的版本号
     *
     * @param groupId 组ID
     * @param rows 返回结果数量，默认10
     * @return 最新版本号，如果未找到则返回null
     */
    fun getLatestVersionByGroupId(groupId: String, rows: Int = 10): String? {
        val artifacts = searchByGroupId(groupId, rows)
        return if (artifacts.isNotEmpty()) {
            // 返回第一个工件的最新版本（API默认按相关性排序）
            artifacts.firstOrNull()?.latestVersion
        } else {
            null
        }
    }


    /**
     * 通过groupId搜索该组下的所有工件
     *
     * @param groupId 组ID
     * @param rows 返回结果数量，默认20
     * @return 工件列表
     */
    fun searchByGroupId(groupId: String, rows: Int = 20): List<MavenArtifact> {
        return searchArtifacts("g:$groupId", rows)
    }

    /**
     * 通过artifactId搜索工件（跨组搜索）
     *
     * @param artifactId 工件ID
     * @param rows 返回结果数量，默认20
     * @return 工件列表
     */
    fun searchByArtifactId(artifactId: String, rows: Int = 20): List<MavenArtifact> {
        return searchArtifacts("a:$artifactId", rows)
    }

    /**
     * 通过groupId和artifactId精确搜索工件
     *
     * @param groupId 组ID
     * @param artifactId 工件ID
     * @param rows 返回结果数量，默认20
     * @return 工件列表
     */
    fun searchByCoordinates(groupId: String, artifactId: String, rows: Int = 20): List<MavenArtifact> {
        return searchArtifacts("g:$groupId AND a:$artifactId", rows)
    }

    /**
     * 通用搜索方法
     *
     * @param query 搜索查询条件
     * @param rows 返回结果数量，默认20
     * @return 工件列表
     */
    private fun searchArtifacts(query: String, rows: Int = 20): List<MavenArtifact> {
        val encodedQuery = query.replace(" ", "%20")
        val url = "$SEARCH_API_BASE?q=$encodedQuery&rows=$rows&wt=json"
        return try {
            val jsonResponse = fetchUrl(url) ?: return emptyList()
            parseSearchResponse(jsonResponse)
        } catch (e: Exception) {
            println("搜索失败: ${e.message}")
            emptyList()
        }
    }

    /**
     * 发送HTTP请求获取内容
     */
    private fun fetchUrl(url: String): String? {
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            return if (response.isSuccessful) response.body?.string() else null
        }
    }

    /**
     * 解析搜索响应JSON
     */
    private fun parseSearchResponse(jsonContent: String): List<MavenArtifact> {
        try {
            val response: SearchResponse = objectMapper.readValue(jsonContent)
            return response.response?.docs?.map { doc ->
                MavenArtifact(
                    id = doc.id ?: "",
                    groupId = doc.g ?: "",
                    artifactId = doc.a ?: "",
                    version = doc.v ?: "",
                    latestVersion = doc.latestVersion ?: doc.v ?: "",
                    packaging = doc.p ?: "jar",
                    timestamp = doc.timestamp ?: 0L,
                    repositoryId = doc.repositoryId ?: "central"
                )
            } ?: emptyList()
        } catch (e: Exception) {
            println("解析搜索响应失败: ${e.message}")
            return emptyList()
        }
    }
}

/**
 * Maven工件数据类
 */
data class MavenArtifact(
    val id: String,
    val groupId: String,
    val artifactId: String,
    val version: String,
    val latestVersion: String,
    val packaging: String,
    val timestamp: Long,
    val repositoryId: String
)

/**
 * 搜索API响应结构
 */
internal data class SearchResponse(
    val response: ResponseWrapper?
)

internal data class ResponseWrapper(
    val docs: List<ArtifactDoc>?
)

internal data class ArtifactDoc(
    val id: String?,
    val g: String?,          // groupId
    val a: String?,          // artifactId
    val v: String?,          // version
    val latestVersion: String?,
    val p: String?,          // packaging
    val timestamp: Long?,
    val repositoryId: String?
)
