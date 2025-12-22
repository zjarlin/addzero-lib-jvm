package site.addzero.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpringYmlUtilTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test getYmlResource supports both yml and yaml extensions`() {
        // 创建测试文件
        val ymlFile = File(tempDir, "config.yml")
        ymlFile.writeText("yml: content")

        val yamlFile = File(tempDir, "other.yaml")
        yamlFile.writeText("yaml: content")

        val util = SpringYmlUtil(tempDir.absolutePath)

        // 测试1：查找 .yml 文件
        val found1 = util.getYmlResource("config.yml")
        assertNotNull(found1)
        assertTrue(found1.exists())
        assertEquals("yml: content", found1.readText())

        // 测试2：查找 .yaml 文件
        val found2 = util.getYmlResource("other.yaml")
        assertNotNull(found2)
        assertTrue(found2.exists())
        assertEquals("yaml: content", found2.readText())

        // 测试3：无扩展名时优先查找 .yml
        val found3 = util.getYmlResource("config")
        assertEquals(ymlFile, found3)

        // 测试4：无扩展名时查找 yaml 文件
        val found4 = util.getYmlResource("other")
        assertEquals(yamlFile, found4)

        // 测试5：错误的扩展名返回尝试的文件
        val found5 = util.getYmlResource("nonexistent.txt")
        assertEquals(File(tempDir, "nonexistent.txt"), found5)
    }

    @Test
    fun `test when both yml and yaml exist, yml is preferred`() {
        // 创建同名的两个文件
        val ymlFile = File(tempDir, "test.yml")
        ymlFile.writeText("yml version")

        val yamlFile = File(tempDir, "test.yaml")
        yamlFile.writeText("yaml version")

        val util = SpringYmlUtil(tempDir.absolutePath)

        // 查找无扩展名的文件，应该返回 .yml
        val found = util.getYmlResource("test")
        assertEquals(ymlFile, found)
        assertEquals("yml version", found.readText())
    }

    // 使用私有方法的测试方式
    private fun SpringYmlUtil.getYmlResource(resourceName: String): File {
        val method = SpringYmlUtil::class.java.getDeclaredMethod(
            "getYmlResource",
            String::class.java
        )
        method.isAccessible = true
        return method.invoke(this, resourceName) as File
    }
}