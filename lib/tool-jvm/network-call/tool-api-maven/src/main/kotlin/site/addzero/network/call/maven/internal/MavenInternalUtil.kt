import okhttp3.OkHttpClient
import okhttp3.Request
import org.xml.sax.InputSource
import site.addzero.network.call.maven.internal.MavenArtifactTool
import java.io.StringReader
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory

/**
 * 基于 OkHttp 的 Maven 最新版本查询工具类
 * 支持自定义仓库，默认使用阿里云镜像
 */
internal class MavenVersionFetcher(
    // 仓库列表（默认阿里云优先，兼容国内网络）
    private val repositories: List<String> = listOf(
        "https://maven.aliyun.com/repository/public",
        "https://maven.aliyun.com/repository/central",
        "https://repo1.maven.org/maven2"
    )
) {
    // 初始化 OkHttp 客户端（设置超时和重试）
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * 查询最新版本
     * @param groupId 工件组ID（如 "org.springframework.boot"）
     * @param artifactId 工件ID（如 "spring-boot-starter"）
     * @return 最新版本号，查询失败返回 null
     */
    fun getLatestVersion(groupId: String, artifactId: String): String? {
        // 将 groupId 中的 "." 转换为 "/"（符合 Maven 仓库路径规范）
        val groupPath = groupId.replace('.', '/')

        // 依次尝试每个仓库
        for (repo in repositories) {
            try {
                // 构建 maven-metadata.xml 的 URL
                val metadataUrl = "$repo/$groupPath/$artifactId/maven-metadata.xml"
                // 发送请求获取 XML 内容
                val xmlContent = fetchXml(metadataUrl) ?: continue
                // 解析 XML 提取最新版本
                val latestVersion = parseLatestVersion(xmlContent)
                if (latestVersion.isNotBlank()) {
                    return latestVersion
                }
            } catch (e: Exception) {
                // 单个仓库失败，继续尝试下一个
                println("仓库 $repo 查询失败: ${e.message}")
            }
        }
        return null
    }

    /**
     * 发送 HTTP 请求获取元数据 XML
     */
    private fun fetchXml(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/xml")
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                return response.body?.string()
            }
            return null
        }
    }

    /**
     * 解析 XML 内容，提取 <latest> 标签中的版本号
     */
    private fun parseLatestVersion(xmlContent: String): String {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(InputSource(StringReader(xmlContent)))
        // 忽略 XML 命名空间，直接获取 <latest> 节点内容
        val latestNode = document.getElementsByTagName("latest").item(0)
        return latestNode?.textContent?.trim() ?: ""
    }
}

// 测试示例
