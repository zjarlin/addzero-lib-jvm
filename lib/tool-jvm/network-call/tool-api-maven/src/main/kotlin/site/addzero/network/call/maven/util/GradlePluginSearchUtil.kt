package site.addzero.network.call.maven.util

import java.net.URI

/**
 * Gradle Plugin Portal 搜索工具类
 * 
 * 用于查询 Gradle 插件的版本信息
 * 参考: https://plugins.gradle.org/
 * 
 * 支持的功能:
 * - 通过插件 ID 获取最新版本
 * - 查询插件的 maven-metadata.xml
 * 
 * @author zjarlin
 * @since 2025-12-07
 */
object GradlePluginSearchUtil {

    /**
     * 获取 Gradle 插件的最新版本
     * 
     * 通过 Gradle Plugin Portal 的 Maven 仓库 API 查询插件的 maven-metadata.xml
     * 提取 `<latest>` 或 `<release>` 标签的值作为最新版本
     * 
     * 示例:
     * ```kotlin
     * val version = GradlePluginSearchUtil.getLatestVersion("org.jetbrains.kotlin.jvm")
     * println(version) // 输出: "1.9.20" (示例)
     * ```
     * 
     * @param pluginId Gradle 插件 ID，例如 "org.jetbrains.kotlin.jvm"
     * @param connectTimeout 连接超时时间（毫秒），默认 5000ms
     * @param readTimeout 读取超时时间（毫秒），默认 5000ms
     * @return 最新版本号，如果查询失败则返回 null
     */
    fun getLatestVersion(
        pluginId: String,
        connectTimeout: Int = 5000,
        readTimeout: Int = 5000
    ): String? {
        return try {
            // 构建 Maven metadata URL
            // 例如: https://plugins.gradle.org/m2/org/jetbrains/kotlin/jvm/org.jetbrains.kotlin.jvm.gradle.plugin/maven-metadata.xml
            val urlString = buildMetadataUrl(pluginId)
            val uri = URI(urlString)
            val connection = uri.toURL().openConnection()
            
            connection.connectTimeout = connectTimeout
            connection.readTimeout = readTimeout
            
            val xml = connection.getInputStream().bufferedReader().readText()
            
            // 简单的 XML 解析，提取 <latest> 或 <release> 标签
            parseLatestVersion(xml)
        } catch (e: Exception) {
            // 查询失败，返回 null
            // 可能的原因：网络错误、插件不存在、超时等
            null
        }
    }

    /**
     * 构建插件的 maven-metadata.xml URL
     * 
     * @param pluginId 插件 ID
     * @return metadata URL
     */
    private fun buildMetadataUrl(pluginId: String): String {
        val path = pluginId.replace('.', '/')
        return "https://plugins.gradle.org/m2/$path/$pluginId.gradle.plugin/maven-metadata.xml"
    }

    /**
     * 从 maven-metadata.xml 内容中解析最新版本
     * 
     * 优先查找 `<latest>` 标签，如果不存在则查找 `<release>` 标签
     * 
     * @param xml maven-metadata.xml 的内容
     * @return 解析出的版本号，如果解析失败返回 null
     */
    private fun parseLatestVersion(xml: String): String? {
        val latestPattern = Regex("""<latest>([^<]+)</latest>""")
        val releasePattern = Regex("""<release>([^<]+)</release>""")
        
        return latestPattern.find(xml)?.groupValues?.get(1)
            ?: releasePattern.find(xml)?.groupValues?.get(1)
    }
}
