package site.addzero.util

import io.swagger.annotations.ApiModelProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import site.addzero.util.metainfo.MetaInfoUtils
import java.lang.reflect.AnnotatedElement

class FieldDTOTest {

    @Test
    fun `test FieldDTO with ApiModelProperty annotation`() {
        // 创建一个FieldDTO实例
        val fieldDTO = FieldDTO(
            restName = "测试接口",
            restUrl = "/api/test",
            modelName = "测试模块",
            fieldName = "测试字段",
            fieldEng = "testField",
            fieldType = "String",
            fieldLong = "255"
        )

        // 验证属性值
        assertEquals("测试接口", fieldDTO.restName)
        assertEquals("/api/test", fieldDTO.restUrl)
        assertEquals("测试模块", fieldDTO.modelName)
        assertEquals("测试字段", fieldDTO.fieldName)
        assertEquals("testField", fieldDTO.fieldEng)
        assertEquals("String", fieldDTO.fieldType)
        assertEquals("255", fieldDTO.fieldLong)
    }

    @Test
    fun `test FieldDTO default values`() {
        // 创建一个默认的FieldDTO实例
        val fieldDTO = FieldDTO()

        // 验证默认值
        assertEquals(null, fieldDTO.restName)
        assertEquals(null, fieldDTO.restUrl)
        assertEquals(null, fieldDTO.modelName)
        assertEquals(null, fieldDTO.fieldName)
        assertEquals(null, fieldDTO.fieldEng)
        assertEquals(null, fieldDTO.fieldType)
        assertEquals(null, fieldDTO.fieldLong)
    }

    @Test
    fun `test FieldDTO copy and modify`() {
        // 创建一个FieldDTO实例
        val originalFieldDTO = FieldDTO(
            restName = "原始接口",
            fieldName = "原始字段"
        )

        // 使用copy创建新实例并修改字段
        val modifiedFieldDTO = originalFieldDTO.copy(restName = "修改后的接口")

        // 验证原始实例未被修改
        assertEquals("原始接口", originalFieldDTO.restName)
        assertEquals("原始字段", originalFieldDTO.fieldName)

        // 验证新实例具有修改后的值
        assertEquals("修改后的接口", modifiedFieldDTO.restName)
        assertEquals("原始字段", modifiedFieldDTO.fieldName)
    }

    @Test
    fun `test MetaInfoUtils guessDescription for FieldDTO fields`() {
        // 获取FieldDTO类的restName字段
        val fieldDTOClass = FieldDTO::class.java
        val restNameField = fieldDTOClass.getDeclaredField("restName")
        val description = MetaInfoUtils.guessDescription(restNameField)
        println("Guessed description: $description")
        // 验证描述是否正确获取
        assertEquals("接口名称", description)
    }
}
