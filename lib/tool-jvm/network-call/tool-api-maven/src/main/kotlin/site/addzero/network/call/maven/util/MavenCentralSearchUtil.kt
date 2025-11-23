package site.addzero.network.call.maven.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.Response
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_DOWNLOAD_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_ALL_VERSIONS_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_BY_ARTIFACT_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_BY_CLASS_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_BY_COORDINATES_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_BY_FC_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_BY_GROUP_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_BY_KEYWORD_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_BY_SHA1_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_BY_TAG_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_TEMPLATE
import site.addzero.network.call.maven.util.MavenCentralApiTemplates.CURL_SEARCH_WITH_CORE_TEMPLATE
import site.addzero.util.CurlExecutor

/**
 * 基于Maven Central REST API的搜索工具类
 * 参考: https://central.sonatype.org/search/rest-api-guide/
 *
 * 使用 CurlExecutor 执行curl命令进行API调用
 *
 * 支持的搜索类型:
 * - 按groupId搜索
 * - 按artifactId搜索
 * - 按坐标(groupId + artifactId)搜索
 * - 按所有版本搜索(GAV core)
 * - 按类名搜索
 * - 按完全限定类名搜索
 * - 按SHA-1校验和搜索
 * - 按标签搜索
 */
object MavenCentralSearchUtil {

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
        return artifacts.firstOrNull()?.latestVersion
    }

    /**
     * 通过groupId和artifactId获取最新版本号
     *
     * @param groupId 组ID
     * @param artifactId 工件ID
     * @return 最新版本号，如果未找到则返回null
     */
    fun getLatestVersion(groupId: String, artifactId: String): String? {
        val artifacts = searchByCoordinates(groupId, artifactId, 1)
        return artifacts.firstOrNull()?.latestVersion
    }

    /**
     * 通过groupId搜索该组下的所有工件
     * 返回每个工件的最新版本信息
     *
     * @param groupId 组ID
     * @param rows 返回结果数量，默认20
     * @return 工件列表
     */
    fun searchByGroupId(groupId: String, rows: Int = 20): List<MavenArtifact> {
        val curl = CURL_SEARCH_BY_GROUP_TEMPLATE
            .replace("{{groupId}}", groupId)
            .replace("{{rows}}", rows.toString())
        return executeCurlAndParse(curl)
    }

    /**
     * 通过artifactId搜索工件（跨组搜索）
     * 返回每个工件的最新版本信息
     *
     * @param artifactId 工件ID
     * @param rows 返回结果数量，默认20
     * @return 工件列表
     */
    fun searchByArtifactId(artifactId: String, rows: Int = 20): List<MavenArtifact> {
        val curl = CURL_SEARCH_BY_ARTIFACT_TEMPLATE
            .replace("{{artifactId}}", artifactId)
            .replace("{{rows}}", rows.toString())
        return executeCurlAndParse(curl)
    }

    /**
     * 通过groupId和artifactId精确搜索工件
     * 返回该工件的最新版本信息
     *
     * @param groupId 组ID
     * @param artifactId 工件ID
     * @param rows 返回结果数量，默认20
     * @return 工件列表
     */
    fun searchByCoordinates(groupId: String, artifactId: String, rows: Int = 20): List<MavenArtifact> {
        val curl = CURL_SEARCH_BY_COORDINATES_TEMPLATE
            .replace("{{groupId}}", groupId)
            .replace("{{artifactId}}", artifactId)
            .replace("{{rows}}", rows.toString())
        return executeCurlAndParse(curl)
    }

    /**
     * 搜索特定坐标的所有版本
     * 使用GAV core返回按版本排序的完整列表
     *
     * @param groupId 组ID
     * @param artifactId 工件ID
     * @param rows 返回结果数量，默认20
     * @return 工件版本列表（按版本排序）
     */
    fun searchAllVersions(groupId: String, artifactId: String, rows: Int = 20): List<MavenArtifact> {
        val curl = CURL_SEARCH_ALL_VERSIONS_TEMPLATE
            .replace("{{groupId}}", groupId)
            .replace("{{artifactId}}", artifactId)
            .replace("{{rows}}", rows.toString())
        return executeCurlAndParse(curl)
    }

    /**
     * 按完整坐标搜索（包括版本、打包方式、分类器）
     *
     * @param groupId 组ID
     * @param artifactId 工件ID
     * @param version 版本号（可选）
     * @param packaging 打包方式（可选，如jar、war等）
     * @param classifier 分类器（可选，如javadoc、sources等）
     * @param rows 返回结果数量，默认20
     * @return 工件列表
     */
    fun searchByFullCoordinates(
        groupId: String,
        artifactId: String,
        version: String? = null,
        packaging: String? = null,
        classifier: String? = null,
        rows: Int = 20
    ): List<MavenArtifact> {
        val conditions = mutableListOf("g:$groupId", "a:$artifactId")
        version?.let { conditions.add("v:$it") }
        packaging?.let { conditions.add("p:$it") }
        classifier?.let { conditions.add("l:$it") }
        val query = conditions.joinToString("+AND+")
        
        val curl = CURL_SEARCH_TEMPLATE
            .replace("{{query}}", query)
            .replace("{{rows}}", rows.toString())
            .replace("{{wt}}", "json")
        return executeCurlAndParse(curl)
    }

    /**
     * 按类名搜索包含该类的工件
     *
     * @param className 类名（不含包名）
     * @param rows 返回结果数量，默认20
     * @return 包含该类的工件列表
     */
    fun searchByClassName(className: String, rows: Int = 20): List<MavenArtifact> {
        val curl = CURL_SEARCH_BY_CLASS_TEMPLATE
            .replace("{{className}}", className)
            .replace("{{rows}}", rows.toString())
        return executeCurlAndParse(curl)
    }

    /**
     * 按完全限定类名搜索包含该类的工件
     *
     * @param fullyQualifiedClassName 完全限定类名（包含包名）
     * @param rows 返回结果数量，默认20
     * @return 包含该类的工件列表
     */
    fun searchByFullyQualifiedClassName(fullyQualifiedClassName: String, rows: Int = 20): List<MavenArtifact> {
        val curl = CURL_SEARCH_BY_FC_TEMPLATE
            .replace("{{fullyQualifiedClassName}}", fullyQualifiedClassName)
            .replace("{{rows}}", rows.toString())
        return executeCurlAndParse(curl)
    }

    /**
     * 按SHA-1校验和搜索工件
     *
     * @param sha1 SHA-1校验和（40位十六进制字符串）
     * @param rows 返回结果数量，默认20
     * @return 匹配的工件列表
     */
    fun searchBySha1(sha1: String, rows: Int = 20): List<MavenArtifact> {
        val curl = CURL_SEARCH_BY_SHA1_TEMPLATE
            .replace("{{sha1}}", sha1)
            .replace("{{rows}}", rows.toString())
        return executeCurlAndParse(curl)
    }

    /**
     * 按标签搜索工件
     *
     * @param tag 标签（如sbtplugin、sbtVersion-0.11、scalaVersion-2.9等）
     * @param rows 返回结果数量，默认20
     * @return 匹配的工件列表
     */
    fun searchByTag(tag: String, rows: Int = 20): List<MavenArtifact> {
        val curl = CURL_SEARCH_BY_TAG_TEMPLATE
            .replace("{{tag}}", tag)
            .replace("{{rows}}", rows.toString())
        return executeCurlAndParse(curl)
    }

    /**
     * 关键词搜索（模拟基本搜索框）
     *
     * @param keyword 关键词
     * @param rows 返回结果数量，默认20
     * @return 匹配的工件列表
     */
    fun searchByKeyword(keyword: String, rows: Int = 20): List<MavenArtifact> {
        val curl = CURL_SEARCH_BY_KEYWORD_TEMPLATE
            .replace("{{keyword}}", keyword)
            .replace("{{rows}}", rows.toString())
        return executeCurlAndParse(curl)
    }

    /**
     * 下载指定路径的文件
     *
     * @param groupId 组ID
     * @param artifactId 工件ID
     * @param version 版本号
     * @param filename 文件名（如guice-3.0.0.pom）
     * @return 文件内容的字节数组，如果下载失败则返回null
     */
    fun downloadFile(groupId: String, artifactId: String, version: String, filename: String): ByteArray? {
        val filepath = "${groupId.replace('.', '/')}/$artifactId/$version/$filename"
        val curl = CURL_DOWNLOAD_TEMPLATE.replace("{{filepath}}", filepath)
        return try {
            val response = CurlExecutor.execute(curl)
            if (response.isSuccessful) {
                response.body?.bytes()
            } else {
                println("下载文件失败: HTTP ${response.code}")
                null
            }
        } catch (e: Exception) {
            println("下载文件失败: ${e.message}")
            null
        }
    }

    /**
     * 生成curl命令（用于调试和日志记录）
     *
     * @param query 搜索查询条件
     * @param rows 返回结果数量
     * @param core 搜索核心（可选，使用"gav"获取所有版本）
     * @return curl命令字符串
     */
    fun generateCurlCommand(query: String, rows: Int = 20, core: String? = null): String {
        return if (core != null) {
            CURL_SEARCH_WITH_CORE_TEMPLATE
                .replace("{{query}}", query)
                .replace("{{rows}}", rows.toString())
                .replace("{{wt}}", "json")
                .replace("{{core}}", core)
        } else {
            CURL_SEARCH_TEMPLATE
                .replace("{{query}}", query)
                .replace("{{rows}}", rows.toString())
                .replace("{{wt}}", "json")
        }
    }

    // ========== 内部辅助方法 ==========

    /**
     * 执行curl命令并解析响应为MavenArtifact列表
     *
     * @param curl curl命令字符串
     * @return 工件列表
     */
    private fun executeCurlAndParse(curl: String): List<MavenArtifact> {
        return try {
            val response = CurlExecutor.execute(curl)
            if (response.isSuccessful) {
                val jsonContent = response.body?.string()
                if (jsonContent != null) {
                    parseSearchResponse(jsonContent)
                } else {
                    emptyList()
                }
            } else {
                println("API请求失败: HTTP ${response.code} - ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            println("搜索失败: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 解析搜索响应JSON
     */
    private fun parseSearchResponse(jsonContent: String): List<MavenArtifact> {
        return try {
            val response: SearchResponse = objectMapper.readValue(jsonContent)
            response.response?.docs?.map { doc ->
                MavenArtifact(
                    id = doc.id ?: "",
                    groupId = doc.g ?: "",
                    artifactId = doc.a ?: "",
                    version = doc.v ?: "",
                    latestVersion = doc.latestVersion ?: doc.v ?: "",
                    packaging = doc.p ?: "jar",
                    timestamp = doc.timestamp ?: 0L,
                    repositoryId = doc.repositoryId ?: "central",
                    classifier = doc.l,
                    text = doc.text
                )
            } ?: emptyList()
        } catch (e: Exception) {
            println("解析搜索响应失败: ${e.message}")
            e.printStackTrace()
            emptyList()
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
    val repositoryId: String,
    val classifier: String? = null,
    val text: List<String>? = null
)

/**
 * 搜索API响应结构
 */
internal data class SearchResponse(
    val response: ResponseWrapper?
)

internal data class ResponseWrapper(
    val docs: List<ArtifactDoc>?,
    val numFound: Long?,
    val start: Long?
)

internal data class ArtifactDoc(
    val id: String?,
    val g: String?,          // groupId
    val a: String?,          // artifactId
    val v: String?,          // version
    val latestVersion: String?,
    val p: String?,          // packaging
    val timestamp: Long?,
    val repositoryId: String?,
    val l: String?,          // classifier
    val text: List<String>?  // 文本信息
)
