package site.addzero.apt.dict.processor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import site.addzero.apt.dict.processor.generator.DictFieldInfo

/**
 * 嵌套类前缀生成测试
 */
class NestedClassPrefixTest {
    
    @Test
    fun `test nested class prefix generation`() {
        // 测试根级字段
        val rootField = DictFieldInfo(
            fieldName = "gender",
            fieldType = "String",
            translationFieldName = "genderName",
            dictType = "system",
            dictConfig = "sys_user_sex",
            nestedClassPrefix = "",
            fieldPath = "gender"
        )
        
        assertEquals("", rootField.nestedClassPrefix)
        assertEquals("gender", rootField.fieldPath)
        
        // 测试一级嵌套字段
        val nestedField = DictFieldInfo(
            fieldName = "deviceId",
            fieldType = "String",
            translationFieldName = "deviceIdName",
            dictType = "table",
            dictConfig = "equipment|id|code|",
            nestedClassPrefix = "ComplexNestedEntity.DeviceInfo",
            fieldPath = "deviceInfo.deviceId"
        )
        
        assertEquals("ComplexNestedEntity.DeviceInfo", nestedField.nestedClassPrefix)
        assertEquals("deviceInfo.deviceId", nestedField.fieldPath)
        
        // 测试深度嵌套字段
        val deepNestedField = DictFieldInfo(
            fieldName = "testvar1",
            fieldType = "String",
            translationFieldName = "testvar1Name",
            dictType = "system",
            dictConfig = "sys_normal_disable",
            nestedClassPrefix = "ComplexNestedEntity.DeviceInfo.Location",
            fieldPath = "deviceInfo.location.testvar1"
        )
        
        assertEquals("ComplexNestedEntity.DeviceInfo.Location", deepNestedField.nestedClassPrefix)
        assertEquals("deviceInfo.location.testvar1", deepNestedField.fieldPath)
    }
    
    @Test
    fun `test field path parsing`() {
        val testCases = listOf(
            "gender" to listOf("gender"),
            "deviceInfo.deviceId" to listOf("deviceInfo", "deviceId"),
            "deviceInfo.location.testvar1" to listOf("deviceInfo", "location", "testvar1"),
            "deviceInfo.sensors.sensorId" to listOf("deviceInfo", "sensors", "sensorId")
        )
        
        testCases.forEach { (fieldPath, expectedParts) ->
            val parts = fieldPath.split(".")
            assertEquals(expectedParts, parts, "Field path parsing failed for: $fieldPath")
        }
    }
    
    @Test
    fun `test accessor code generation logic`() {
        // 模拟生成器中的访问器代码生成逻辑
        fun generateAccessorCode(fieldPath: String, objectName: String): String {
            val parts = fieldPath.split(".")
            if (parts.size == 1) {
                val capitalizedName = parts[0].replaceFirstChar { it.uppercase() }
                return "$objectName.get$capitalizedName()"
            } else {
                var accessorChain = objectName
                parts.forEach { part ->
                    val capitalizedPart = part.replaceFirstChar { it.uppercase() }
                    accessorChain = "$accessorChain.get$capitalizedPart()"
                }
                return accessorChain
            }
        }
        
        // 测试简单字段
        assertEquals("dto.getGender()", generateAccessorCode("gender", "dto"))
        
        // 测试嵌套字段
        assertEquals("dto.getDeviceInfo().getDeviceId()", generateAccessorCode("deviceInfo.deviceId", "dto"))
        
        // 测试深度嵌套字段
        assertEquals(
            "dto.getDeviceInfo().getLocation().getTestvar1()", 
            generateAccessorCode("deviceInfo.location.testvar1", "dto")
        )
    }
    
    @Test
    fun `test setter code generation logic`() {
        // 模拟生成器中的设置器代码生成逻辑
        fun generateSetterCode(fieldPath: String, objectName: String, valueName: String): String {
            val parts = fieldPath.split(".")
            if (parts.size == 1) {
                val capitalizedName = parts[0].replaceFirstChar { it.uppercase() }
                return "$objectName.set$capitalizedName($valueName)"
            } else {
                val nestedParts = parts.dropLast(1)
                val fieldName = parts.last()
                
                var accessorChain = objectName
                nestedParts.forEach { part ->
                    val capitalizedPart = part.replaceFirstChar { it.uppercase() }
                    accessorChain = "$accessorChain.get$capitalizedPart()"
                }
                
                val capitalizedFieldName = fieldName.replaceFirstChar { it.uppercase() }
                return "if ($accessorChain != null) { $accessorChain.set$capitalizedFieldName($valueName); }"
            }
        }
        
        // 测试简单字段
        assertEquals("dto.setGenderName(translatedValue)", generateSetterCode("genderName", "dto", "translatedValue"))
        
        // 测试嵌套字段
        assertEquals(
            "if (dto.getDeviceInfo() != null) { dto.getDeviceInfo().setDeviceIdName(translatedValue); }", 
            generateSetterCode("deviceInfo.deviceIdName", "dto", "translatedValue")
        )
        
        // 测试深度嵌套字段
        assertEquals(
            "if (dto.getDeviceInfo().getLocation() != null) { dto.getDeviceInfo().getLocation().setTestvar1Name(translatedValue); }", 
            generateSetterCode("deviceInfo.location.testvar1Name", "dto", "translatedValue")
        )
    }
}