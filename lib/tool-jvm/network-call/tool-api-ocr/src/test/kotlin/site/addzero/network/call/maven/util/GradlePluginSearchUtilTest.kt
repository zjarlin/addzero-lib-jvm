package site.addzero.network.call.maven.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Gradle Plugin Portal 搜索工具测试类
 *
 * @author zjarlin
 * @since 2025-12-07
 */
class GradlePluginSearchUtilTest {

    @Test
    fun `测试获取真实插件的最新版本`() {

        println("\n========== 测试获取真实插件的最新版本 ==========")

        val pluginId = "org.jetbrains.kotlin.jvm"
        val version = GradlePluginSearchUtil.getLatestVersion(pluginId, connectTimeout = 10000, readTimeout = 10000)

        println("插件 ID: $pluginId")
        println("获取到的版本: $version")

        assertNotNull(version, "应该能够获取到 Kotlin JVM 插件的版本")
        assertTrue(version!!.isNotEmpty(), "版本号不应该为空")
        println("✓ 成功获取到版本: $version")
    }

    @Test
    fun `测试获取不存在插件的最新版本`() {

        println("\n========== 测试获取不存在插件的最新版本 ==========")

        val pluginId = "com.example.nonexistent.plugin"
        val version = GradlePluginSearchUtil.getLatestVersion(pluginId, connectTimeout = 5000, readTimeout = 5000)

        println("插件 ID: $pluginId")
        println("获取到的版本: $version")

        assertNull(version, "不存在的插件应该返回 null")
        println("✓ 正确处理了不存在的插件")
    }

    @Test
    fun `测试获取最新版本 - 超时设置`() {
        println("\n========== 测试获取最新版本 - 超时设置 ==========")

        val pluginId = "org.jetbrains.kotlin.jvm"

        // 使用非常短的超时时间，确保会超时
        val version = GradlePluginSearchUtil.getLatestVersion(
            pluginId,
            connectTimeout = 1,  // 1ms，几乎肯定会超时
            readTimeout = 1
        )

        println("插件 ID: $pluginId")
        println("使用极短超时时间获取到的版本: $version")

        // 在超时情况下应该返回 null
        assertNull(version, "超时时应该返回 null")
        println("✓ 正确处理了超时情况")
    }
}
