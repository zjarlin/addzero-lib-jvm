package site.addzero.apt.dict.processor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * 嵌套类前缀构建测试
 */
class NestedClassPrefixBuildingTest {
    
    @Test
    fun `test nested class prefix patterns`() {
        // 测试不同的嵌套类命名模式
        val testCases = listOf(
            // Java 内部类模式
            TestCase(
                qualifiedName = "org.test.device.enty.ComplexNestedEntity\$DeviceInfo",
                rootClassName = "ComplexNestedEntity",
                expectedPrefix = "ComplexNestedEntity"
            ),
            
            TestCase(
                qualifiedName = "org.test.device.enty.ComplexNestedEntity\$DeviceInfo\$Location",
                rootClassName = "ComplexNestedEntity",
                expectedPrefix = "ComplexNestedEntity.DeviceInfo"
            ),
            
            // Kotlin 嵌套类模式
            TestCase(
                qualifiedName = "org.test.device.enty.ComplexNestedEntity.DeviceInfo",
                rootClassName = "ComplexNestedEntity",
                expectedPrefix = "ComplexNestedEntity"
            ),
            
            TestCase(
                qualifiedName = "org.test.device.enty.ComplexNestedEntity.DeviceInfo.Location",
                rootClassName = "ComplexNestedEntity",
                expectedPrefix = "ComplexNestedEntity.DeviceInfo"
            ),
            
            // 根类情况
            TestCase(
                qualifiedName = "org.test.device.enty.ComplexNestedEntity",
                rootClassName = "ComplexNestedEntity",
                expectedPrefix = ""
            )
        )
        
        testCases.forEach { testCase ->
            val result = buildNestedClassPrefixFromQualifiedName(
                testCase.qualifiedName,
                testCase.rootClassName
            )
            assertEquals(
                testCase.expectedPrefix,
                result,
                "Failed for qualified name: ${testCase.qualifiedName}"
            )
        }
    }
    
    @Test
    fun `test package name extraction`() {
        val testCases = mapOf(
            "org.test.device.enty.ComplexNestedEntity" to "org.test.device.enty",
            "org.test.device.enty.ComplexNestedEntity\$DeviceInfo" to "org.test.device.enty",
            "org.test.device.enty.ComplexNestedEntity.DeviceInfo.Location" to "org.test.device.enty",
            "SimpleClass" to "",
            "" to ""
        )
        
        testCases.forEach { (qualifiedName, expectedPackage) ->
            val result = extractPackageNameFromQualifiedName(qualifiedName)
            assertEquals(expectedPackage, result, "Failed for: $qualifiedName")
        }
    }
    
    @Test
    fun `test field path generation`() {
        // 测试字段路径的生成
        val testCases = listOf(
            FieldPathTestCase(
                fieldName = "gender",
                nestedPrefix = "",
                expectedFieldPath = "gender"
            ),
            
            FieldPathTestCase(
                fieldName = "deviceId",
                nestedPrefix = "ComplexNestedEntity.DeviceInfo",
                expectedFieldPath = "deviceInfo.deviceId"
            ),
            
            FieldPathTestCase(
                fieldName = "testvar1",
                nestedPrefix = "ComplexNestedEntity.DeviceInfo.Location",
                expectedFieldPath = "deviceInfo.location.testvar1"
            )
        )
        
        testCases.forEach { testCase ->
            val result = generateFieldPath(testCase.fieldName, testCase.nestedPrefix)
            assertEquals(
                testCase.expectedFieldPath,
                result,
                "Failed for field: ${testCase.fieldName} with prefix: ${testCase.nestedPrefix}"
            )
        }
    }
    
    /**
     * 模拟嵌套类前缀构建逻辑
     */
    private fun buildNestedClassPrefixFromQualifiedName(qualifiedName: String, rootClassName: String): String {
        if (qualifiedName.isEmpty()) return ""
        
        val packageName = extractPackageNameFromQualifiedName(qualifiedName)
        val classPath = qualifiedName.removePrefix("$packageName.")
        
        // 如果类路径就是根类名，返回空前缀
        if (classPath == rootClassName) {
            return ""
        }
        
        return when {
            // Java 内部类：使用 $ 分隔符
            classPath.contains("$") -> {
                val nestedPath = classPath.replace("$", ".")
                if (nestedPath.startsWith(rootClassName)) {
                    val parts = nestedPath.split(".")
                    if (parts.size > 1) {
                        parts.dropLast(1).joinToString(".")
                    } else {
                        rootClassName
                    }
                } else {
                    "$rootClassName.${classPath.substringAfterLast("$")}"
                }
            }
            
            // Kotlin 嵌套类：使用 . 分隔符
            classPath.contains(".") -> {
                if (classPath.startsWith(rootClassName)) {
                    val parts = classPath.split(".")
                    if (parts.size > 1) {
                        parts.dropLast(1).joinToString(".")
                    } else {
                        rootClassName
                    }
                } else {
                    "$rootClassName.${classPath.substringAfterLast(".")}"
                }
            }
            
            // 简单情况
            else -> {
                rootClassName
            }
        }
    }
    
    /**
     * 模拟包名提取逻辑
     */
    private fun extractPackageNameFromQualifiedName(qualifiedName: String): String {
        if (qualifiedName.isEmpty()) return ""
        
        // 处理内部类的情况，先将 $ 替换为 .
        val normalizedName = qualifiedName.replace("$", ".")
        val parts = normalizedName.split(".")
        
        // 找到第一个大写字母开头的部分（通常是类名）
        val classNameIndex = parts.indexOfFirst { part ->
            part.isNotEmpty() && part[0].isUpperCase()
        }
        
        return if (classNameIndex > 0) {
            parts.take(classNameIndex).joinToString(".")
        } else {
            ""
        }
    }
    
    /**
     * 生成字段路径
     */
    private fun generateFieldPath(fieldName: String, nestedPrefix: String): String {
        if (nestedPrefix.isEmpty()) {
            return fieldName
        }
        
        // 从嵌套前缀生成字段路径
        val prefixParts = nestedPrefix.split(".")
        if (prefixParts.size <= 1) {
            return fieldName
        }
        
        // 跳过根类名，将其余部分转换为小驼峰命名
        val pathParts = prefixParts.drop(1).map { it.replaceFirstChar { char -> char.lowercase() } }
        return (pathParts + fieldName).joinToString(".")
    }
    
    data class TestCase(
        val qualifiedName: String,
        val rootClassName: String,
        val expectedPrefix: String
    )
    
    data class FieldPathTestCase(
        val fieldName: String,
        val nestedPrefix: String,
        val expectedFieldPath: String
    )
}