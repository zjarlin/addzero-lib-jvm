package site.addzero.apt.dict.processor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import site.addzero.dict.trans.inter.DictTranslationConfig
import java.util.concurrent.CompletableFuture

/**
 * 懒加载翻译机制测试
 */
class LazyLoadingTranslationTest {
    
    @Test
    fun `test DictTranslationConfig creation and properties`() {
        val config = DictTranslationConfig(
            fieldPath = "userType",
            dictType = "system",
            dictConfig = "user_type",
            targetFieldPath = "userTypeName",
            nestedClassPrefix = ""
        )
        
        assertEquals("userType", config.fieldPath)
        assertEquals("system", config.dictType)
        assertEquals("user_type", config.dictConfig)
        assertEquals("userTypeName", config.targetFieldPath)
        assertEquals("", config.nestedClassPrefix)
        
        assertEquals("system:user_type", config.getConfigKey())
        assertTrue(config.isSystemDict())
        assertFalse(config.isTableDict())
    }
    
    @Test
    fun `test DictTranslationConfig grouping by type`() {
        val configs = listOf(
            DictTranslationConfig(
                fieldPath = "field1",
                dictType = "system",
                dictConfig = "dict1",
                targetFieldPath = "field1Name"
            ),
            DictTranslationConfig(
                fieldPath = "field2",
                dictType = "system",
                dictConfig = "dict1", // 相同配置
                targetFieldPath = "field2Name"
            ),
            DictTranslationConfig(
                fieldPath = "field3",
                dictType = "table",
                dictConfig = "table1|code|name|",
                targetFieldPath = "field3Name"
            )
        )
        
        // 按配置键分组
        val groupedConfigs = configs.groupBy { it.getConfigKey() }
        
        // 验证分组结果
        assertEquals(2, groupedConfigs.size)
        assertTrue(groupedConfigs.containsKey("system:dict1"))
        assertTrue(groupedConfigs.containsKey("table:table1|code|name|"))
        
        // system:dict1 组应该有2个配置
        assertEquals(2, groupedConfigs["system:dict1"]?.size)
        assertEquals(1, groupedConfigs["table:table1|code|name|"]?.size)
    }
    
    @Test
    fun `test nested field path configuration`() {
        val nestedConfig = DictTranslationConfig(
            fieldPath = "deviceInfo.location.testvar1",
            dictType = "system",
            dictConfig = "sys_normal_disable",
            targetFieldPath = "deviceInfo.location.testvar1Name",
            nestedClassPrefix = "ComplexNestedEntity.DeviceInfo.Location"
        )
        
        assertEquals("deviceInfo.location.testvar1", nestedConfig.fieldPath)
        assertEquals("deviceInfo.location.testvar1Name", nestedConfig.targetFieldPath)
        assertEquals("ComplexNestedEntity.DeviceInfo.Location", nestedConfig.nestedClassPrefix)
        assertEquals("system:sys_normal_disable", nestedConfig.getConfigKey())
        assertTrue(nestedConfig.isSystemDict())
    }
    
    @Test
    fun `test table dict configuration`() {
        val tableConfig = DictTranslationConfig(
            fieldPath = "productKey",
            dictType = "table",
            dictConfig = "iot_product|product_key|product_name|status='1'",
            targetFieldPath = "productKeyName"
        )
        
        assertEquals("table", tableConfig.dictType)
        assertEquals("iot_product|product_key|product_name|status='1'", tableConfig.dictConfig)
        assertTrue(tableConfig.isTableDict())
        assertFalse(tableConfig.isSystemDict())
    }
    
    /**
     * 测试用DTO类
     */
    data class TestDTO(
        var field1Name: String? = null,
        var field2Name: String? = null,
        var field3Name: String? = null
    )
}