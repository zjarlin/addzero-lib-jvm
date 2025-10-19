package site.addzero.network.call.maven

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import site.addzero.network.call.maven.exception.MavenRepositoryException

class MavenRepositoryClientTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `test search by group id`() {
        // 测试按groupId搜索
        val artifacts = MavenRepositoryClient.searchByGroupId("com.fasterxml.jackson.core", 10)
        assertNotNull(artifacts)
        assertFalse(artifacts.isEmpty()) { "应该能搜索到com.fasterxml.jackson.core组的构件" }
        
        // 验证返回的构件信息
        val artifact = artifacts.first()
        assertEquals("com.fasterxml.jackson.core", artifact.groupId)
        assertNotNull(artifact.artifactId)
        assertNotNull(artifact.latestVersion)
    }

    @Test
    fun `test search by artifact id`() {
        // 测试按artifactId搜索
        val artifacts = MavenRepositoryClient.searchByArtifactId("junit-jupiter", 10)
        assertNotNull(artifacts)
        assertFalse(artifacts.isEmpty()) { "应该能搜索到junit-jupiter构件" }
        
        // 验证返回的构件信息
        val artifact = artifacts.first()
        assertEquals("junit-jupiter", artifact.artifactId)
        assertNotNull(artifact.groupId)
        assertNotNull(artifact.latestVersion)
    }

    @Test
    fun `test search by group id and artifact id`() {
        // 测试按groupId和artifactId搜索
        val artifacts = MavenRepositoryClient.searchByGroupIdAndArtifactId("org.junit.jupiter", "junit-jupiter", 10)
        assertNotNull(artifacts)
        assertFalse(artifacts.isEmpty()) { "应该能搜索到org.junit.jupiter:junit-jupiter构件" }
        
        // 验证返回的构件信息
        val artifact = artifacts.first()
        assertEquals("org.junit.jupiter", artifact.groupId)
        assertEquals("junit-jupiter", artifact.artifactId)
        assertNotNull(artifact.latestVersion)
    }

    @Test
    fun `test search by pattern`() {
        // 测试按模式搜索
        val artifacts = MavenRepositoryClient.searchByPattern("org.springframework", "spring-context*", 10)
        assertNotNull(artifacts)
        
        // 如果有返回结果，验证格式
        if (artifacts.isNotEmpty()) {
            val artifact = artifacts.first()
            assertEquals("org.springframework", artifact.groupId)
            assertTrue(artifact.artifactId.startsWith("spring-context")) { "构件ID应该以spring-context开头" }
            assertNotNull(artifact.latestVersion)
        }
    }

    @Test
    fun `test search by keyword`() {
        // 测试按关键字搜索
        val artifacts = MavenRepositoryClient.searchByKeyword("kotlin-stdlib", 10)
        assertNotNull(artifacts)
        assertFalse(artifacts.isEmpty()) { "应该能搜索到kotlin-stdlib相关的构件" }
    }

    @Test
    fun `test get group artifacts`() {
        // 测试获取组内所有构件
        val artifactIds = MavenRepositoryClient.getGroupArtifacts("org.jetbrains.kotlin")
        assertNotNull(artifactIds)
        assertFalse(artifactIds.isEmpty()) { "应该能获取到org.jetbrains.kotlin组内的构件列表" }
        
        // 验证返回的是artifactId列表
        assertTrue(artifactIds.all { it.isNotBlank() }) { "所有返回的都应该是非空的artifactId" }
    }

    @Test
    fun `test get group artifacts with pattern`() {
        // 测试按模式获取组内构件
        val artifactIds = MavenRepositoryClient.getGroupArtifactsWithPattern("org.jetbrains.kotlin", "kotlin-stdlib*")
        assertNotNull(artifactIds)
        
        // 如果有返回结果，验证格式
        if (artifactIds.isNotEmpty()) {
            assertTrue(artifactIds.all { it.startsWith("kotlin-stdlib") }) { "所有返回的artifactId应该以kotlin-stdlib开头" }
        }
    }

    @Test
    fun `test search with blank group id throws exception`() {
        // 测试groupId为空时抛出异常
        assertThrows(IllegalArgumentException::class.java) {
            MavenRepositoryClient.searchByGroupId("")
        }
    }

    @Test
    fun `test search with blank artifact id throws exception`() {
        // 测试artifactId为空时抛出异常
        assertThrows(IllegalArgumentException::class.java) {
            MavenRepositoryClient.searchByArtifactId("")
        }
    }

    @Test
    fun `test coordinate methods`() {
        // 测试坐标方法
        val artifacts = MavenRepositoryClient.searchByGroupId("org.junit.jupiter", 1)
        assertFalse(artifacts.isEmpty()) { "应该能搜索到至少一个构件用于测试" }
        
        val artifact = artifacts.first()
        val coordinate = artifact.getCoordinate()
        val coordinateWithVersion = artifact.getCoordinateWithVersion()
        
        assertTrue(coordinate.contains(":")) { "坐标应该包含冒号分隔符" }
        assertTrue(coordinate.startsWith("${artifact.groupId}:")) { "坐标应该以groupId开头" }
        assertTrue(coordinate.endsWith(":${artifact.artifactId}")) { "坐标应该以artifactId结尾" }
        
        if (artifact.latestVersion != null) {
            assertTrue(coordinateWithVersion.endsWith(":${artifact.latestVersion}")) { "带版本的坐标应该以版本号结尾" }
        }
    }

    @Test
    fun `test search response methods`() {
        // 测试搜索响应方法
        val artifacts = MavenRepositoryClient.searchByGroupId("com.fasterxml.jackson.core", 5)
        assertFalse(artifacts.isEmpty()) { "应该能搜索到至少一个构件用于测试" }
        
        // 注意：由于这是静态方法调用，我们无法直接测试MavenSearchResponse的方法
        // 但可以通过验证返回的数据来间接测试
        assertTrue(artifacts.size <= 5) { "返回的结果数不应超过请求数量" }
        assertNotNull(artifacts.first().id) { "每个构件都应该有ID" }
    }
}