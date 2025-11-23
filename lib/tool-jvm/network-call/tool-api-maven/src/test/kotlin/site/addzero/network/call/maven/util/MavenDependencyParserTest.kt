package site.addzero.network.call.maven.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Maven 依赖解析器测试
 */
class MavenDependencyParserTest {

    @Test
    fun `测试解析标准 Maven 依赖标签`() {
        println("\n========== 测试解析标准 Maven 依赖标签 ==========")
        
        val dependencyXml = """
            <dependency>
                <groupId>com.google.inject</groupId>
                <artifactId>guice</artifactId>
                <version>4.2.3</version>
            </dependency>
        """.trimIndent()
        
        val coordinate = MavenDependencyParser.parseDependency(dependencyXml)
        
        assertNotNull(coordinate, "应该成功解析依赖")
        assertEquals("com.google.inject", coordinate.groupId)
        assertEquals("guice", coordinate.artifactId)
        assertEquals("4.2.3", coordinate.version)
        
        println("原始 XML:")
        println(dependencyXml)
        println("\n解析结果:")
        println("  groupId: ${coordinate.groupId}")
        println("  artifactId: ${coordinate.artifactId}")
        println("  version: ${coordinate.version}")
    }

    @Test
    fun `测试解析带 scope 的依赖`() {
        println("\n========== 测试解析带 scope 的依赖 ==========")
        
        val dependencyXml = """
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>4.12</version>
                <scope>test</scope>
            </dependency>
        """.trimIndent()
        
        val coordinate = MavenDependencyParser.parseDependency(dependencyXml)
        
        assertNotNull(coordinate)
        assertEquals("junit", coordinate.groupId)
        assertEquals("junit", coordinate.artifactId)
        assertEquals("4.12", coordinate.version)
        assertEquals("test", coordinate.scope)
        
        println("依赖坐标: ${coordinate.toMavenCoordinate()}")
        println("scope: ${coordinate.scope}")
    }

    @Test
    fun `测试解析紧凑格式的 XML`() {
        println("\n========== 测试解析紧凑格式的 XML ==========")
        
        val compactXml = "<dependency><groupId>com.fasterxml.jackson.core</groupId><artifactId>jackson-databind</artifactId><version>2.14.0</version></dependency>"
        
        val coordinate = MavenDependencyParser.parseDependencyFromCompactXml(compactXml)
        
        assertNotNull(coordinate)
        println("紧凑 XML: $compactXml")
        println("解析结果: ${coordinate.toMavenCoordinate()}")
    }

    @Test
    fun `测试从 Maven 坐标字符串解析`() {
        println("\n========== 测试从 Maven 坐标字符串解析 ==========")
        
        val coordinate1 = MavenDependencyParser.parseDependencyFromCoordinate("com.google.inject:guice:4.2.3")
        val coordinate2 = MavenDependencyParser.parseDependencyFromCoordinate("junit:junit:4.12:test")
        
        assertNotNull(coordinate1)
        assertEquals("com.google.inject", coordinate1.groupId)
        assertEquals("guice", coordinate1.artifactId)
        assertEquals("4.2.3", coordinate1.version)
        
        assertNotNull(coordinate2)
        assertEquals("test", coordinate2.scope)
        
        println("坐标 1: ${coordinate1.toMavenCoordinate()}")
        println("坐标 2: ${coordinate2.toMavenCoordinate()}, scope: ${coordinate2.scope}")
    }

    @Test
    fun `测试更新到最新版本 - guice`() {
        println("\n========== 测试更新到最新版本: guice ==========")
        
        val originalXml = """
            <dependency>
                <groupId>com.google.inject</groupId>
                <artifactId>guice</artifactId>
                <version>4.2.3</version>
            </dependency>
        """.trimIndent()
        
        println("原始 XML:")
        println(originalXml)
        
        val updatedXml = MavenDependencyParser.updateToLatestVersion(originalXml)
        
        println("\n更新后的 XML:")
        println(updatedXml)
        
        // 解析新旧版本
        val oldCoordinate = MavenDependencyParser.parseDependency(originalXml)
        val newCoordinate = MavenDependencyParser.parseDependency(updatedXml)
        
        println("\n版本对比:")
        println("  旧版本: ${oldCoordinate?.version}")
        println("  新版本: ${newCoordinate?.version}")
    }

    @Test
    fun `测试批量更新依赖`() {
        println("\n========== 测试批量更新依赖 ==========")
        
        val dependencies = listOf(
            """
            <dependency>
                <groupId>com.google.inject</groupId>
                <artifactId>guice</artifactId>
                <version>4.2.3</version>
            </dependency>
            """.trimIndent(),
            """
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>4.12</version>
                <scope>test</scope>
            </dependency>
            """.trimIndent()
        )
        
        println("批量更新 ${dependencies.size} 个依赖")
        
        val results = MavenDependencyParser.batchUpdateToLatestVersion(dependencies)
        
        println("\n更新结果:")
        results.forEachIndexed { index, result ->
            println("\n[${index + 1}] ${result.summary}")
            if (result.isUpdated) {
                println("状态: 已更新")
            } else {
                println("状态: 未变化")
            }
        }
        
        assertEquals(dependencies.size, results.size)
    }

    @Test
    fun `测试从 pom_xml 中提取依赖`() {
        println("\n========== 测试从 pom.xml 中提取依赖 ==========")
        
        val pomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <dependencies>
                    <dependency>
                        <groupId>com.google.inject</groupId>
                        <artifactId>guice</artifactId>
                        <version>4.2.3</version>
                    </dependency>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                        <version>4.12</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
        """.trimIndent()
        
        val dependencies = MavenDependencyParser.extractDependenciesFromPom(pomXml)
        
        println("从 pom.xml 中提取到 ${dependencies.size} 个依赖:")
        dependencies.forEachIndexed { index, dep ->
            val coordinate = MavenDependencyParser.parseDependency(dep)
            println("  [${index + 1}] ${coordinate?.toMavenCoordinate()}")
        }
        
        assertTrue(dependencies.size >= 2, "应该提取到至少 2 个依赖")
    }

    @Test
    fun `测试更新整个 pom_xml 到最新版本`() {
        println("\n========== 测试更新整个 pom.xml 到最新版本 ==========")
        
        val originalPom = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
                <dependencies>
                    <dependency>
                        <groupId>com.google.inject</groupId>
                        <artifactId>guice</artifactId>
                        <version>4.2.3</version>
                    </dependency>
                </dependencies>
            </project>
        """.trimIndent()
        
        println("原始 pom.xml:")
        println(originalPom)
        
        val updatedPom = MavenDependencyParser.updatePomToLatestVersions(originalPom)
        
        println("\n更新后的 pom.xml:")
        println(updatedPom)
    }

    @Test
    fun `测试获取 pom 更新报告`() {
        println("\n========== 测试获取 pom 更新报告 ==========")
        
        val pomXml = """
            <dependencies>
                <dependency>
                    <groupId>com.google.inject</groupId>
                    <artifactId>guice</artifactId>
                    <version>4.2.3</version>
                </dependency>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                    <version>4.12</version>
                    <scope>test</scope>
                </dependency>
            </dependencies>
        """.trimIndent()
        
        val report = MavenDependencyParser.getPomUpdateReport(pomXml)
        
        println("依赖更新报告:")
        report.forEach { result ->
            println("  ${result.summary}")
        }
        
        assertTrue(report.isNotEmpty(), "应该有更新报告")
    }

    @Test
    fun `测试转换为 Maven XML 格式`() {
        println("\n========== 测试转换为 Maven XML 格式 ==========")
        
        val coordinate = MavenDependencyParser.MavenDependencyCoordinate(
            groupId = "com.google.inject",
            artifactId = "guice",
            version = "5.1.0",
            scope = "compile"
        )
        
        val xml = coordinate.toMavenXml()
        
        println("生成的 Maven XML:")
        println(xml)
        
        assertTrue(xml.contains("<groupId>com.google.inject</groupId>"))
        assertTrue(xml.contains("<artifactId>guice</artifactId>"))
        assertTrue(xml.contains("<version>5.1.0</version>"))
    }

    @Test
    fun `测试验证依赖 XML 格式`() {
        println("\n========== 测试验证依赖 XML 格式 ==========")
        
        val validXml = """
            <dependency>
                <groupId>com.google.inject</groupId>
                <artifactId>guice</artifactId>
                <version>4.2.3</version>
            </dependency>
        """.trimIndent()
        
        val invalidXml = "<dependency>invalid</dependency>"
        
        val isValid1 = MavenDependencyParser.isValidDependencyXml(validXml)
        val isValid2 = MavenDependencyParser.isValidDependencyXml(invalidXml)
        
        println("有效的 XML: $isValid1")
        println("无效的 XML: $isValid2")
        
        assertTrue(isValid1, "应该是有效的依赖 XML")
        assertTrue(!isValid2, "应该是无效的依赖 XML")
    }

    @Test
    fun `测试格式化依赖 XML`() {
        println("\n========== 测试格式化依赖 XML ==========")
        
        val uglyXml = "<dependency><groupId>com.google.inject</groupId><artifactId>guice</artifactId><version>4.2.3</version></dependency>"
        
        println("原始（紧凑）XML:")
        println(uglyXml)
        
        val formattedXml = MavenDependencyParser.formatDependencyXml(uglyXml)
        
        println("\n格式化后的 XML:")
        println(formattedXml)
        
        assertNotNull(formattedXml)
    }

    @Test
    fun `测试从 Gradle 格式转换为 Maven 格式`() {
        println("\n========== 测试从 Gradle 格式转换为 Maven 格式 ==========")
        
        val gradleDependencies = listOf(
            """implementation("com.google.inject:guice:4.2.3")""",
            """testImplementation("junit:junit:4.12")""",
            """compileOnly("org.projectlombok:lombok:1.18.24")"""
        )
        
        println("Gradle 依赖转换为 Maven XML:")
        gradleDependencies.forEach { gradle ->
            println("\nGradle: $gradle")
            val mavenXml = MavenDependencyParser.convertFromGradle(gradle)
            println("Maven:")
            println(mavenXml)
        }
    }

    @Test
    fun `测试实际场景 - 更新项目 pom_xml`() {
        println("\n========== 测试实际场景 - 更新项目 pom.xml ==========")
        
        val projectPom = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>demo</artifactId>
                <version>1.0.0</version>
                
                <dependencies>
                    <dependency>
                        <groupId>com.google.inject</groupId>
                        <artifactId>guice</artifactId>
                        <version>4.2.3</version>
                    </dependency>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                        <version>4.12</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
        """.trimIndent()
        
        println("检查依赖更新...")
        
        val report = MavenDependencyParser.getPomUpdateReport(projectPom)
        
        println("\n可更新的依赖:")
        report.filter { it.isUpdated }.forEach { result ->
            println("  ${result.summary}")
        }
        
        println("\n已是最新的依赖:")
        report.filter { !it.isUpdated }.forEach { result ->
            println("  ${result.summary}")
        }
    }
}
