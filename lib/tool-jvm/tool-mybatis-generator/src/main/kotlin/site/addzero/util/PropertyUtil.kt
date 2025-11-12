package site.addzero.util

import cn.hutool.core.io.resource.ClassPathResource
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

/**
 * 简单的配置文件读取工具，避免在各处重复解析classpath下的properties。
 * todo 到时候独立出模块
 */
object PropertyUtil {
    private val cache = ConcurrentHashMap<String, Properties>()

    fun getProperty(fileName: String, key: String): String =
        getProperties(fileName).getProperty(key)
            ?: throw IllegalArgumentException("请在${fileName}中配置 ${key}")

    fun getPropertyOrNull(fileName: String, key: String): String? =
        getProperties(fileName).getProperty(key)

    fun getProperties(fileName: String): Properties = cache.getOrPut(fileName) { load(fileName) }

    private fun load(fileName: String): Properties {
        val stream = ClassPathResource(fileName).stream
            ?: throw IllegalArgumentException("类路径下不存在配置文件：$fileName")
        return Properties().apply {
            stream.use { load(it) }
        }
    }
}
