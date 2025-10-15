@file:JvmName("MavenRepositoryClients")

package site.addzero.maven

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import site.addzero.maven.exception.MavenRepositoryConnectionException
import site.addzero.maven.exception.MavenRepositoryException
import site.addzero.maven.exception.MavenRepositoryParseException
import site.addzero.maven.model.MavenArtifact
import site.addzero.maven.model.MavenSearchResponse
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Maven仓库客户端
 *
 * 提供Maven中央仓库搜索功能，支持按groupId、artifactId、关键字搜索
 *
 * @author zjarlin
 * @since 2025/10/15
 */
object MavenRepositoryClient {

    private val logger = LoggerFactory.getLogger(MavenRepositoryClient::class.java)

    private const val MAVEN_SEARCH_BASE_URL = "https://search.maven.org/solrsearch/select"
    private const val DEFAULT_ROWS = 50
    private const val DEFAULT_START = 0

    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 按groupId搜索构件
     *
     * @param groupId 组ID
     * @param rows 返回结果数量，默认50
     * @param start 起始位置，默认0
     * @return 构件列表
     */
    @JvmStatic
    @JvmOverloads
    fun searchByGroupId(groupId: String, rows: Int = DEFAULT_ROWS, start: Int = DEFAULT_START): List<MavenArtifact> {
        require(groupId.isNotBlank()) { "GroupId cannot be blank" }

        val query = "g:$groupId"
        return executeSearch(query, rows, start)
    }

    /**
     * 按artifactId搜索构件
     *
     * @param artifactId 构件ID
     * @param rows 返回结果数量，默认50
     * @param start 起始位置，默认0
     * @return 构件列表
     */
    @JvmStatic
    @JvmOverloads
    fun searchByArtifactId(artifactId: String, rows: Int = DEFAULT_ROWS, start: Int = DEFAULT_START): List<MavenArtifact> {
        require(artifactId.isNotBlank()) { "ArtifactId cannot be blank" }

        val query = "a:$artifactId"
        return executeSearch(query, rows, start)
    }

    /**
     * 按groupId和artifactId精确搜索构件
     *
     * @param groupId 组ID
     * @param artifactId 构件ID
     * @param rows 返回结果数量，默认50
     * @param start 起始位置，默认0
     * @return 构件列表
     */
    @JvmStatic
    @JvmOverloads
    fun searchByGroupIdAndArtifactId(groupId: String, artifactId: String, rows: Int = DEFAULT_ROWS, start: Int = DEFAULT_START): List<MavenArtifact> {
        require(groupId.isNotBlank()) { "GroupId cannot be blank" }
        require(artifactId.isNotBlank()) { "ArtifactId cannot be blank" }

        val query = "g:$groupId AND a:$artifactId"
        return executeSearch(query, rows, start)
    }

    /**
     * 按通配符搜索构件
     * 例如：searchByPattern("cn.hutool", "hutool-*")
     *
     * @param groupId 组ID，支持通配符*
     * @param artifactIdPattern 构件ID模式，支持通配符*
     * @param rows 返回结果数量，默认50
     * @param start 起始位置，默认0
     * @return 构件列表
     */
    @JvmStatic
    @JvmOverloads
    fun searchByPattern(groupId: String, artifactIdPattern: String, rows: Int = DEFAULT_ROWS, start: Int = DEFAULT_START): List<MavenArtifact> {
        require(groupId.isNotBlank()) { "GroupId cannot be blank" }
        require(artifactIdPattern.isNotBlank()) { "ArtifactId pattern cannot be blank" }

        val query = "g:$groupId AND a:$artifactIdPattern"
        return executeSearch(query, rows, start)
    }

    /**
     * 按关键字搜索构件
     *
     * @param keyword 关键字
     * @param rows 返回结果数量，默认50
     * @param start 起始位置，默认0
     * @return 构件列表
     */
    @JvmStatic
    @JvmOverloads
    fun searchByKeyword(keyword: String, rows: Int = DEFAULT_ROWS, start: Int = DEFAULT_START): List<MavenArtifact> {
        require(keyword.isNotBlank()) { "Keyword cannot be blank" }

        val query = keyword
        return executeSearch(query, rows, start)
    }

    /**
     * 获取指定组的所有模块
     * 这是对原始curl命令功能的直接实现
     *
     * @param groupId 组ID
     * @return 构件ID列表
     */
    @JvmStatic
    fun getGroupArtifacts(groupId: String): List<String> {
        return searchByGroupId(groupId, rows = 200).map { it.artifactId }
    }

    /**
     * 获取指定组的所有模块，按指定模式过滤
     * 这是对原始curl命令功能的扩展实现
     *
     * @param groupId 组ID
     * @param artifactIdPattern 构件ID模式，支持通配符*
     * @return 构件ID列表
     */
    @JvmStatic
    fun getGroupArtifactsWithPattern(groupId: String, artifactIdPattern: String): List<String> {
        return searchByPattern(groupId, artifactIdPattern, rows = 200).map { it.artifactId }
    }

    /**
     * 执行搜索请求
     */
    private fun executeSearch(query: String, rows: Int, start: Int): List<MavenArtifact> {
        val url = MAVEN_SEARCH_BASE_URL.toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("rows", rows.toString())
            .addQueryParameter("start", start.toString())
            .addQueryParameter("wt", "json")
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        logger.debug("Executing Maven search: {}", url)

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw MavenRepositoryConnectionException(
                        "Maven search failed with code: ${response.code}, message: ${response.message}"
                    )
                }

                val responseBody = response.body?.string()
                    ?: throw MavenRepositoryParseException("Empty response body")

                logger.debug("Maven search response: {}", responseBody)

                val searchResponse = objectMapper.readValue<MavenSearchResponse>(responseBody)
                return searchResponse.getArtifacts()
            }
        } catch (e: IOException) {
            throw MavenRepositoryConnectionException("Failed to connect to Maven repository", e)
        } catch (e: Exception) {
            if (e is MavenRepositoryException) {
                throw e
            }
            throw MavenRepositoryParseException("Failed to parse Maven search response", e)
        }
    }

    /**
     * 关闭客户端，释放资源
     */
    @JvmStatic
    fun shutdown() {
        httpClient.dispatcher.executorService.shutdown()
        httpClient.connectionPool.evictAll()
    }
}
