package site.addzero.network.call.maven.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Gradle 依赖解析器测试
 */
class GradleDependencyParserTest {

    @Test
    fun `测试解析双引号格式的依赖`() {
        println("\n========== 测试解析双引号格式的依赖 ==========")
        
        val dependencyString = """implementation("com.google.inject:guice:4.2.3")"""
        val coordinate = GradleDependencyParser.parseDependency(dependencyString)
        
        assertNotNull(coordinate, "应该成功解析依赖")
        assertEquals("com.google.inject", coordinate.groupId)
        assertEquals("guice", coordinate.artifactId)
        assertEquals("4.2.3", coordinate.version)
        assertEquals("implementation", coordinate.configuration)
        
        println("原始字符串: $dependencyString")
        println("解析结果:")
        println("  groupId: ${coordinate.groupId}")
        println("  artifactId: ${coordinate.artifactId}")
        println("  version: ${coordinate.version}")
        println("  configuration: ${coordinate.configuration}")
    }

    @Test
    fun `测试解析单引号格式的依赖`() {
        println("\n========== 测试解析单引号格式的依赖 ==========")
        
        val dependencyString = """implementation('com.fasterxml.jackson.core:jackson-databind:2.14.0')"""
        val coordinate = GradleDependencyParser.parseDependency(dependencyString)
        
        assertNotNull(coordinate)
        assertEquals("com.fasterxml.jackson.core", coordinate.groupId)
        assertEquals("jackson-databind", coordinate.artifactId)
        assertEquals("2.14.0", coordinate.version)
        
        println("原始字符串: $dependencyString")
        println("解析结果: ${coordinate.toMavenCoordinate()}")
    }

    @Test
    fun `测试解析不带括号的依赖`() {
        println("\n========== 测试解析不带括号的依赖 ==========")
        
        val dependencyString = """api "org.springframework.boot:spring-boot-starter:3.0.0""""
        val coordinate = GradleDependencyParser.parseDependency(dependencyString)
        
        assertNotNull(coordinate)
        assertEquals("api", coordinate.configuration)
        println("原始字符串: $dependencyString")
        println("解析结果: ${coordinate.toMavenCoordinate()}")
    }

    @Test
    fun `测试更新到最新版本 - guice`() {
        println("\n========== 测试更新到最新版本: guice ==========")
        
        val originalString = """implementation("com.google.inject:guice:4.2.3")"""
        println("原始依赖: $originalString")
        
        val updatedString = GradleDependencyParser.updateToLatestVersion(originalString)
        println("更新后的依赖: $updatedString")
        
        // 验证格式正确
        assertTrue(updatedString.contains("com.google.inject:guice:"))
        assertTrue(updatedString.startsWith("implementation(\""))
        assertTrue(updatedString.endsWith("\")"))
        
        // 解析新旧版本
        val oldCoordinate = GradleDependencyParser.parseDependency(originalString)
        val newCoordinate = GradleDependencyParser.parseDependency(updatedString)
        
        println("\n版本对比:")
        println("  旧版本: ${oldCoordinate?.version}")
        println("  新版本: ${newCoordinate?.version}")
    }

    @Test
    fun `测试更新到最新版本 - jackson-databind`() {
        println("\n========== 测试更新到最新版本: jackson-databind ==========")
        
        val originalString = """implementation('com.fasterxml.jackson.core:jackson-databind:2.14.0')"""
        println("原始依赖: $originalString")
        
        val updatedString = GradleDependencyParser.updateToLatestVersion(originalString)
        println("更新后的依赖: $updatedString")
        
        // 验证使用单引号
        assertTrue(updatedString.contains("'"))
        assertFalse(updatedString.contains("\""))
        
        val oldCoordinate = GradleDependencyParser.parseDependency(originalString)
        val newCoordinate = GradleDependencyParser.parseDependency(updatedString)
        
        println("\n版本对比:")
        println("  旧版本: ${oldCoordinate?.version}")
        println("  新版本: ${newCoordinate?.version}")
    }

    @Test
    fun `测试批量更新依赖`() {
        println("\n========== 测试批量更新依赖 ==========")
        
        val dependencies = listOf(
            """implementation("com.google.inject:guice:4.2.3")""",
            """testImplementation("junit:junit:4.12")""",
            """api('org.springframework.boot:spring-boot-starter:2.7.0')"""
        )
        
        println("批量更新 ${dependencies.size} 个依赖:")
        dependencies.forEach { println("  - $it") }
        
        val results = GradleDependencyParser.batchUpdateToLatestVersion(dependencies)
        
        println("\n更新结果:")
        results.forEachIndexed { index, result ->
            println("\n[${index + 1}] ${result.coordinate?.toMavenCoordinate()}")
            println("    原始: ${result.original}")
            println("    更新: ${result.updated}")
            println("    状态: ${if (result.isUpdated) "已更新" else "未变化"}")
            if (result.isUpdated) {
                println("    版本: ${result.oldVersion} -> ${result.newVersion}")
            }
        }
        
        // 验证结果数量
        assertEquals(dependencies.size, results.size)
    }

    @Test
    fun `测试提取 Maven 坐标`() {
        println("\n========== 测试提取 Maven 坐标 ==========")
        
        val testCases = listOf(
            """implementation("com.google.inject:guice:4.2.3")""",
            """api('org.springframework:spring-core:5.3.0')""",
            """testImplementation "junit:junit:4.13.2""""
        )
        
        println("从 Gradle 依赖提取 Maven 坐标:")
        testCases.forEach { dependency ->
            val coordinate = GradleDependencyParser.extractMavenCoordinate(dependency)
            println("  $dependency")
            println("    -> $coordinate")
        }
    }

    @Test
    fun `测试验证依赖字符串格式`() {
        println("\n========== 测试验证依赖字符串格式 ==========")
        
        val validCases = listOf(
            """implementation("com.google.inject:guice:4.2.3")""",
            """api('org.springframework:spring-core:5.3.0')""",
            """testImplementation "junit:junit:4.13.2""""
        )
        
        val invalidCases = listOf(
            """implementation("invalid")""",
            """some random text""",
            """implementation(com.google:guice)""",
            ""
        )
        
        println("有效的依赖字符串:")
        validCases.forEach { dep ->
            val isValid = GradleDependencyParser.isValidDependencyString(dep)
            println("  ✓ $dep -> $isValid")
            assertTrue(isValid, "应该是有效的依赖字符串")
        }
        
        println("\n无效的依赖字符串:")
        invalidCases.forEach { dep ->
            val isValid = GradleDependencyParser.isValidDependencyString(dep)
            println("  ✗ \"$dep\" -> $isValid")
            assertFalse(isValid, "应该是无效的依赖字符串")
        }
    }

    @Test
    fun `测试转换为 Gradle 依赖声明`() {
        println("\n========== 测试转换为 Gradle 依赖声明 ==========")
        
        val coordinate = GradleDependencyParser.DependencyCoordinate(
            groupId = "com.google.inject",
            artifactId = "guice",
            version = "7.0.0",
            configuration = "implementation"
        )
        
        val doubleQuote = coordinate.toGradleDependency(useDoubleQuote = true)
        val singleQuote = coordinate.toGradleDependency(useDoubleQuote = false)
        
        println("坐标: ${coordinate.toMavenCoordinate()}")
        println("双引号格式: $doubleQuote")
        println("单引号格式: $singleQuote")
        
        assertEquals("""implementation("com.google.inject:guice:7.0.0")""", doubleQuote)
        assertEquals("""implementation('com.google.inject:guice:7.0.0')""", singleQuote)
    }

    @Test
    fun `测试不存在的依赖 - 保持原样`() {
        println("\n========== 测试不存在的依赖 - 保持原样 ==========")
        
        val originalString = """implementation("com.nonexistent:artifact:1.0.0")"""
        println("原始依赖: $originalString")
        
        val updatedString = GradleDependencyParser.updateToLatestVersion(originalString)
        println("更新后的依赖: $updatedString")
        
        // 不存在的依赖应该保持原样
        println("结果: ${if (originalString == updatedString) "保持不变（符合预期）" else "已更新"}")
    }

    @Test
    fun `测试实际场景 - 更新项目依赖`() {
        println("\n========== 测试实际场景 - 更新项目依赖 ==========")
        
        // 模拟 build.gradle.kts 中的依赖
        val projectDependencies = """
            dependencies {
                implementation("com.google.inject:guice:4.2.3")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
                testImplementation("junit:junit:4.12")
            }
        """.trimIndent()
        
        println("原始依赖配置:")
        println(projectDependencies)
        
        // 提取所有依赖行
        val dependencyLines = projectDependencies.lines()
            .filter { it.contains("implementation") && it.contains(":") }
        
        println("\n检查更新...")
        dependencyLines.forEach { line ->
            val trimmed = line.trim()
            val updated = GradleDependencyParser.updateToLatestVersion(trimmed)
            
            if (trimmed != updated) {
                println("\n需要更新:")
                println("  原始: $trimmed")
                println("  最新: $updated")
            }
        }
    }
}
