package site.addzero.network.call.maven.util

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Maven Central 模糊搜索测试
 * 
 * 包含：
 * - 关键词搜索
 * - 按类名搜索
 * - 按完全限定类名搜索
 * - 按标签搜索
 * - 按 artifactId 搜索（跨组）
 */
class MavenCentralFuzzySearchTest {

    @Test
    fun `测试关键词搜索 - jackson`() {
        println("\n========== 测试关键词搜索: jackson ==========")
        
        val artifacts = MavenCentralSearchUtil.searchByKeyword("jackson", 5)
        
        println("找到 ${artifacts.size} 个结果:")
        artifacts.forEach { artifact ->
            println("  - ${artifact.groupId}:${artifact.artifactId}:${artifact.latestVersion}")
        }
        
        assertTrue(artifacts.isNotEmpty(), "关键词搜索应该返回结果")
        artifacts.forEach { artifact ->
            assertTrue(
                artifact.groupId.contains("jackson", ignoreCase = true) ||
                artifact.artifactId.contains("jackson", ignoreCase = true),
                "结果应包含关键词 jackson"
            )
        }
    }

    @Test
    fun `测试关键词搜索 - spring`() {
        println("\n========== 测试关键词搜索: spring ==========")
        
        val artifacts = MavenCentralSearchUtil.searchByKeyword("spring", 10)
        
        println("找到 ${artifacts.size} 个结果:")
        artifacts.take(5).forEach { artifact ->
            println("  - ${artifact.groupId}:${artifact.artifactId}:${artifact.latestVersion}")
        }
        if (artifacts.size > 5) {
            println("  ... 还有 ${artifacts.size - 5} 个结果")
        }
        
        assertTrue(artifacts.isNotEmpty(), "关键词搜索应该返回结果")
    }

    @Test
    fun `测试按类名搜索 - JUnit`() {
        println("\n========== 测试按类名搜索: JUnit ==========")
        
        val artifacts = MavenCentralSearchUtil.searchByClassName("JUnit", 5)
        
        println("找到 ${artifacts.size} 个包含 JUnit 类的工件:")
        artifacts.forEach { artifact ->
            println("  - ${artifact.groupId}:${artifact.artifactId}:${artifact.latestVersion}")
        }
        
        // 注意: 类名搜索可能返回空结果，取决于 Maven Central 的索引
        println("结果数量: ${artifacts.size}")
    }

    @Test
    fun `测试按完全限定类名搜索`() {
        println("\n========== 测试按完全限定类名搜索: org.junit.Test ==========")
        
        val artifacts = MavenCentralSearchUtil.searchByFullyQualifiedClassName("org.junit.Test", 5)
        
        println("找到 ${artifacts.size} 个包含 org.junit.Test 的工件:")
        artifacts.forEach { artifact ->
            println("  - ${artifact.groupId}:${artifact.artifactId}:${artifact.latestVersion}")
        }
        
        // 注意: 完全限定类名搜索可能返回空结果
        println("结果数量: ${artifacts.size}")
    }

    @Test
    fun `测试按标签搜索 - sbtplugin`() {
        println("\n========== 测试按标签搜索: sbtplugin ==========")
        
        val artifacts = MavenCentralSearchUtil.searchByTag("sbtplugin", 5)
        
        println("找到 ${artifacts.size} 个 SBT 插件:")
        artifacts.forEach { artifact ->
            println("  - ${artifact.groupId}:${artifact.artifactId}:${artifact.latestVersion}")
        }
        
        // SBT 插件搜索结果可能为空
        println("SBT 插件数量: ${artifacts.size}")
    }

    @Test
    fun `测试按 artifactId 搜索 - 跨组搜索 guice`() {
        println("\n========== 测试按 artifactId 搜索: guice ==========")
        
        val artifacts = MavenCentralSearchUtil.searchByArtifactId("guice", 10)
        
        println("找到 ${artifacts.size} 个名为 guice 的工件:")
        artifacts.forEach { artifact ->
            println("  - ${artifact.groupId}:${artifact.artifactId}:${artifact.latestVersion}")
        }
        
        assertTrue(artifacts.isNotEmpty(), "应该找到 guice 相关的工件")
        // 验证每个结果的 artifactId 都包含 guice
        artifacts.forEach { artifact ->
            assertTrue(
                artifact.artifactId.contains("guice", ignoreCase = true),
                "artifactId 应包含 guice"
            )
        }
    }

    @Test
    fun `测试按 artifactId 搜索 - 可能不存在的工件`() {
        println("\n========== 测试按 artifactId 搜索: very-unlikely-artifact-name-xyz123 ==========")
        
        val artifacts = MavenCentralSearchUtil.searchByArtifactId("very-unlikely-artifact-name-xyz123", 5)
        
        println("找到 ${artifacts.size} 个结果")
        
        // 不太可能存在的工件名称应该返回空列表
        assertTrue(artifacts.isEmpty(), "不太可能存在的工件名称应该返回空列表")
    }

    @Test
    fun `测试按 SHA-1 搜索`() {
        println("\n========== 测试按 SHA-1 搜索 ==========")
        
        // 这是一个真实的 SHA-1，对应某个 jar 文件
        val sha1 = "35379fb6526fd019f331542b4e9ae2e566c57933"
        val artifacts = MavenCentralSearchUtil.searchBySha1(sha1, 5)
        
        println("找到 ${artifacts.size} 个匹配的工件:")
        artifacts.forEach { artifact ->
            println("  - ${artifact.groupId}:${artifact.artifactId}:${artifact.version}")
        }
        
        // SHA-1 搜索可能返回空结果，取决于索引
        println("结果数量: ${artifacts.size}")
    }
}
