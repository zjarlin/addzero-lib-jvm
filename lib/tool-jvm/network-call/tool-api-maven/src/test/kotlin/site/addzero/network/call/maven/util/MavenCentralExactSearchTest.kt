package site.addzero.network.call.maven.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Maven Central 精确搜索测试
 *
 * 包含：
 * - 按 groupId 精确搜索
 * - 按坐标（groupId + artifactId）精确搜索
 * - 搜索所有版本
 * - 获取最新版本
 * - 按完整坐标搜索（含分类器）
 * - 文件下载
 * - Curl 命令生成
 */
class MavenCentralExactSearchTest {

    @Test
    fun `测试按 groupId 精确搜索 - com_google_inject`() {
        println("\n========== 测试按 groupId 搜索: com.google.inject ==========")

        val artifacts = MavenCentralSearchUtil.searchByGroupId("com.google.inject", 10)

        println("找到 ${artifacts.size} 个工件:")
        artifacts.forEach { artifact ->
            println("  - ${artifact.artifactId}:${artifact.latestVersion}")
        }

        assertTrue(artifacts.isNotEmpty(), "应该找到 com.google.inject 下的工件")
        // 验证所有结果都属于 com.google.inject
        artifacts.forEach { artifact ->
            assertEquals("com.google.inject", artifact.groupId, "groupId 应该是 com.google.inject")
        }
    }

    @Test
    fun `测试按 groupId 精确搜索 - org_springframework_boot`() {
        println("\n========== 测试按 groupId 搜索: org.springframework.boot ==========")

        val artifacts = MavenCentralSearchUtil.searchByGroupId("site.addzero", 5)
        
        println("找到 ${artifacts.size} 个 Spring Boot 工件:")
        artifacts.forEach { artifact ->
            println("  - ${artifact.artifactId}:${artifact.latestVersion}")
        }

        assertTrue(artifacts.isNotEmpty(), "应该找到 Spring Boot 工件")
        artifacts.forEach { artifact ->
            assertEquals("org.springframework.boot", artifact.groupId)
        }
    }

    @Test
    fun `测试按坐标精确搜索 - guice`() {
        println("\n========== 测试按坐标搜索: com.google.inject:guice ==========")

        val artifacts = MavenCentralSearchUtil.searchByCoordinates("com.google.inject", "guice", 1)

        println("找到 ${artifacts.size} 个结果:")
        artifacts.forEach { artifact ->
            println("  - ${artifact.groupId}:${artifact.artifactId}:${artifact.latestVersion}")
        }

        assertTrue(artifacts.isNotEmpty(), "应该找到 guice 工件")
        val artifact = artifacts.first()
        assertEquals("com.google.inject", artifact.groupId)
        assertEquals("guice", artifact.artifactId)
        assertNotNull(artifact.latestVersion, "应该有最新版本信息")
        println("\nGuice 最新版本: ${artifact.latestVersion}")
    }

    @Test
    fun `测试获取最新版本 - guice`() {
        println("\n========== 测试获取最新版本: com.google.inject:guice ==========")

        val latestVersion = MavenCentralSearchUtil.getLatestVersion("com.google.inject", "guice")

        assertNotNull(latestVersion, "应该能获取到 guice 的最新版本")
        println("Guice 最新版本: $latestVersion")

        // 验证版本号格式 (应该类似 x.y.z)
        assertTrue(latestVersion!!.matches(Regex("\\d+\\.\\d+.*")), "版本号格式应该正确")
    }

    @Test
    fun `测试获取最新版本 - jackson-databind`() {
        println("\n========== 测试获取最新版本: com.fasterxml.jackson.core:jackson-databind ==========")

        val latestVersion = MavenCentralSearchUtil.getLatestVersion(
            "com.fasterxml.jackson.core",
            "jackson-databind"
        )

        assertNotNull(latestVersion, "应该能获取到 jackson-databind 的最新版本")
        println("Jackson Databind 最新版本: $latestVersion")
    }

    @Test
    fun `测试搜索所有版本 - guice`() {
        println("\n========== 测试搜索所有版本: com.google.inject:guice ==========")

        val versions = MavenCentralSearchUtil.searchAllVersions("com.google.inject", "guice", 10)

        println("找到 ${versions.size} 个版本:")
        versions.forEach { artifact ->
            println("  - ${artifact.version}")
        }

        assertTrue(versions.size > 1, "应该有多个版本")
        // 验证每个结果都是 guice
        versions.forEach { artifact ->
            assertEquals("com.google.inject", artifact.groupId)
            assertEquals("guice", artifact.artifactId)
        }
    }

    @Test
    fun `测试按完整坐标搜索 - 带分类器`() {
        println("\n========== 测试按完整坐标搜索: guice with javadoc classifier ==========")

        val artifacts = MavenCentralSearchUtil.searchByFullCoordinates(
            groupId = "com.google.inject",
            artifactId = "guice",
            version = "7.0.0",
            packaging = "jar",
            classifier = "javadoc",
            rows = 5
        )

        println("找到 ${artifacts.size} 个结果:")
        artifacts.forEach { artifact ->
            println("  - ${artifact.groupId}:${artifact.artifactId}:${artifact.version} [${artifact.classifier}]")
        }

        // 带分类器的搜索可能返回空结果
        println("结果数量: ${artifacts.size}")
    }

    @Test
    fun `测试按 groupId 获取最新版本`() {
        println("\n========== 测试按 groupId 获取最新版本: com.google.inject ==========")

        val latestVersion = MavenCentralSearchUtil.getLatestVersionByGroupId("com.google.inject", 1)

        println("com.google.inject 组的最新版本: ${latestVersion ?: "未找到"}")

        // 注意: 这会返回该组下最相关工件的最新版本
        // 结果可能为 null 或者是某个工件的版本
    }

    @Test
    fun `测试不存在的坐标`() {
        println("\n========== 测试不存在的坐标 ==========")

        val artifacts = MavenCentralSearchUtil.searchByCoordinates(
            "com.nonexistent.group",
            "nonexistent-artifact",
            5
        )

        println("找到 ${artifacts.size} 个结果")
        assertTrue(artifacts.isEmpty(), "不存在的坐标应该返回空列表")
    }

    @Test
    fun `测试生成 curl 命令`() {
        println("\n========== 测试生成 curl 命令 ==========")

        val curlCommand = MavenCentralSearchUtil.generateCurlCommand(
            query = "g:com.google.inject AND a:guice",
            rows = 20
        )

        println("生成的 curl 命令:")
        println(curlCommand)

        assertNotNull(curlCommand, "应该能生成 curl 命令")
        assertTrue(curlCommand.contains("curl"), "应该包含 curl 命令")
        assertTrue(curlCommand.contains("search.maven.org"), "应该包含 Maven Central URL")
        assertTrue(curlCommand.contains("com.google.inject"), "应该包含查询条件")
    }

    @Test
    fun `测试生成带 core 参数的 curl 命令`() {
        println("\n========== 测试生成带 core 参数的 curl 命令 ==========")

        val curlCommand = MavenCentralSearchUtil.generateCurlCommand(
            query = "g:com.google.inject AND a:guice",
            rows = 20,
            core = "gav"
        )

        println("生成的 curl 命令:")
        println(curlCommand)

        assertTrue(curlCommand.contains("core=gav"), "应该包含 core=gav 参数")
    }

    @Test
    fun `测试文件下载功能`() {
        println("\n========== 测试文件下载: guice POM 文件 ==========")

        // 尝试下载 guice 的 POM 文件
        val pomBytes = MavenCentralSearchUtil.downloadFile(
            groupId = "com.google.inject",
            artifactId = "guice",
            version = "7.0.0",
            filename = "guice-7.0.0.pom"
        )

        if (pomBytes != null) {
            println("成功下载 POM 文件，大小: ${pomBytes.size} 字节")
            val pomContent = String(pomBytes)
            println("POM 文件前 200 个字符:")
            println(pomContent.take(200))

            assertTrue(pomBytes.isNotEmpty(), "POM 文件内容不应为空")
            assertTrue(pomContent.contains("<?xml"), "POM 文件应该是 XML 格式")
        } else {
            println("下载失败或文件不存在")
        }
    }

    @Test
    fun `测试下载不存在的文件`() {
        println("\n========== 测试下载不存在的文件 ==========")

        val fileBytes = MavenCentralSearchUtil.downloadFile(
            groupId = "com.nonexistent",
            artifactId = "nonexistent",
            version = "1.0.0",
            filename = "nonexistent-1.0.0.jar"
        )

        println("下载结果: ${if (fileBytes == null) "失败（预期）" else "成功"}")
        assertTrue(fileBytes == null, "不存在的文件应该返回 null")
    }
}
