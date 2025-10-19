package site.addzero.network.call.maven.internal

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request
import site.addzero.network.call.maven.entity.ArtifactDetails
import site.addzero.network.call.maven.entity.SearchApiResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * 完整的 Maven 工件工具类
 * 功能：查询版本详情、模糊搜索、版本比较
 * 依赖：OkHttp3（网络请求）、Jackson（JSON/XML解析）
 */
 class MavenArtifactTool(
    // 阿里云仓库列表（国内优先）
    private val repositories: List<String> = listOf(
        "https://maven.aliyun.com/repository/public",
        "https://maven.aliyun.com/repository/central"
    ),
    // 阿里云搜索API
    private val searchApiUrl: String = "https://maven.aliyun.com/artifact/search"
) {
    // 初始化OkHttp客户端
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // 初始化Jackson ObjectMapper（支持Kotlin和XML）
    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    // ------------------------------ 工件版本详情查询 ------------------------------
    /**
     * 查询工件的完整版本信息
     */
    internal fun getArtifactDetails(groupId: String, artifactId: String):
            ArtifactDetails? {
        val groupPath = groupId.replace('.', '/')
        for (repo in repositories) {
            try {
                val metadataUrl = "$repo/$groupPath/$artifactId/maven-metadata.xml"
                val xmlContent = fetchUrl(metadataUrl) ?: continue
                return parseMetadataXml(xmlContent, groupId, artifactId, repo)
            } catch (e: Exception) {
                e.printStackTrace()
                println("仓库 $repo 解析失败: ${e.message}")
            }
        }
        return null
    }

    /**
     * 解析maven-metadata.xml获取版本详情
     */
    private fun parseMetadataXml(
        xmlContent: String,
        groupId: String,
        artifactId: String,
        repoUrl: String
    ): ArtifactDetails {
        // 简化XML解析（实际项目可使用Jackson XML模块）
        val latestVersion = xmlContent.extractXmlTagContent("latest")
        val releaseVersion = xmlContent.extractXmlTagContent("release")
        val lastUpdatedStr = xmlContent.extractXmlTagContent("lastUpdated")
        val versions = xmlContent.extractAllXmlTagContent("version")

        return ArtifactDetails(
            groupId = groupId,
            artifactId = artifactId,
            repositoryUrl = repoUrl,
            latestVersion = latestVersion,
            releaseVersion = releaseVersion,
            allVersions = versions.sortedWith(VersionComparator),
            lastUpdated = lastUpdatedStr?.let { parseLastUpdated(it) },
            versionCount = versions.size
        )
    }

    // ------------------------------ 工件模糊搜索 ------------------------------
    /**
     * 通过artifactId关键词模糊搜索工件
     */
    fun searchArtifacts(
        keyword: String,
        page: Int = 1,
        pageSize: Int = 20
    ): List<SearchResult> {
        val url = "$searchApiUrl?keyword=$keyword&type=artifact&page=$page&pageSize=$pageSize"
        return try {
            val jsonContent = fetchUrl(url) ?: return emptyList()
            parseSearchResults(jsonContent)
        } catch (e: Exception) {
            println("搜索失败: ${e.message}")
            emptyList()
        }
    }

    /**
     * 解析搜索结果JSON
     */
    private fun parseSearchResults(jsonContent: String): List<SearchResult> {
        val response: SearchApiResponse = objectMapper.readValue(jsonContent)
        if (response.code != "200") {
            println("搜索API错误: ${response.message}")
            return emptyList()
        }
        return response.data.list.map { item ->
            SearchResult(
                groupId = item.groupId,
                artifactId = item.artifactId,
                latestVersion = item.version,
                description = item.description ?: "无描述",
                repository = "阿里云仓库"
            )
        }
    }

    // ------------------------------ 通用工具方法 ------------------------------
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
     * 从XML字符串中提取标签内容
     */
    private fun String.extractXmlTagContent(tag: String): String? {
        val regex = Regex("<$tag>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.find(this)?.groupValues?.get(1)?.trim()
    }

    /**
     * 从XML字符串中提取所有标签内容
     */
    private fun String.extractAllXmlTagContent(tag: String): List<String> {
        val regex = Regex("<$tag>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.findAll(this).map { it.groupValues[1].trim() }.toList()
    }

    /**
     * 解析Maven的lastUpdated时间（格式：yyyyMMddHHmmss）
     */
    private fun parseLastUpdated(lastUpdatedStr: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(lastUpdatedStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        } catch (e: Exception) {
            null
        }
    }

    // ------------------------------ 数据类 ------------------------------

    // ------------------------------ 版本比较器 ------------------------------
    private object VersionComparator : Comparator<String> {
        override fun compare(v1: String, v2: String): Int {
            val parts1 = splitVersion(v1)
            val parts2 = splitVersion(v2)
            val maxLength = maxOf(parts1.size, parts2.size)

            for (i in 0 until maxLength) {
                val part1 = parts1.getOrElse(i) { "" }
                val part2 = parts2.getOrElse(i) { "" }

                when {
                    part1.isNumeric() && part2.isNumeric() -> {
                        val num1 = part1.toLong()
                        val num2 = part2.toLong()
                        if (num1 != num2) return num1.compareTo(num2)
                    }

                    else -> {
                        val cmp = part1.compareTo(part2)
                        if (cmp != 0) return cmp
                    }
                }
            }
            return 0
        }

        private fun splitVersion(version: String): List<String> {
            return version.split(Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")).filter { it.isNotBlank() }
        }

        private fun String.isNumeric() = matches(Regex("\\d+"))
    }
}

