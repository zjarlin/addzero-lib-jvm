package site.addzero.apt.dict.processor.generator

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.method.LsiMethod
import site.addzero.util.lsi.type.LsiType

/**
 * DictDtoGenerator测试
 */
class DictDtoGeneratorTest {
    
    @Test
    fun `should generate DTO with correct naming conventions`() {
        // 创建模拟的LsiClass和LsiField
        val mockClass = createMockLsiClass()
        val mockFields = createMockDictFields()
        
        // 生成DTO代码
        val generatedCode = DictDtoGenerator.generateDictDTO(mockClass, mockFields)
        
        // 验证生成的代码
        assertTrue(generatedCode.contains("public class UserDictDTO extends User"))
        assertTrue(generatedCode.contains("private String gender_dictText;"))
        assertTrue(generatedCode.contains("private String productNameTest1;"))
        assertTrue(generatedCode.contains("private String testColumn;"))
        assertTrue(generatedCode.contains("@Data"))
        assertTrue(generatedCode.contains("@EqualsAndHashCode(callSuper = true)"))
    }
    
    @Test
    fun `should handle serializationAlias correctly`() {
        val mockClass = createMockLsiClass()
        val mockFields = listOf(createMockFieldWithSerializationAlias())
        
        val generatedCode = DictDtoGenerator.generateDictDTO(mockClass, mockFields)
        
        // 验证使用了serializationAlias而不是默认命名
        assertTrue(generatedCode.contains("private String productNameTest1;"))
        assertFalse(generatedCode.contains("private String testStr_dictText;"))
    }
    
    @Test
    fun `should convert database column names to camelCase`() {
        val mockClass = createMockLsiClass()
        val mockFields = listOf(createMockFieldWithNameColumn())
        
        val generatedCode = DictDtoGenerator.generateDictDTO(mockClass, mockFields)
        
        // 验证数据库列名转换为驼峰命名
        assertTrue(generatedCode.contains("private String testColumn;"))
        assertFalse(generatedCode.contains("private String test_column;"))
    }
    
    private fun createMockLsiClass(): LsiClass {
        return object : LsiClass {
            override val name: String = "User"
            override val qualifiedName: String = "com.example.User"
            override val comment: String? = null
            override val fields: List<LsiField> = emptyList()
            override val methods: List<LsiMethod> = emptyList()
            override val annotations: List<LsiAnnotation> = emptyList()
            override val isInterface: Boolean = false
            override val isEnum: Boolean = false
            override val isCollectionType: Boolean = false
            override val isPojo: Boolean = true
            override val superClasses: List<LsiClass> = emptyList()
            override val interfaces: List<LsiClass> = emptyList()
        }
    }
    
    private fun createMockDictFields(): List<LsiField> {
        return listOf(
            createMockFieldWithDictText(),
            createMockFieldWithSerializationAlias(),
            createMockFieldWithNameColumn()
        )
    }
    
    private fun createMockFieldWithDictText(): LsiField {
        return object : LsiField {
            override val name: String = "gender"
            override val type: LsiType? = null
            override val typeName: String = "String"
            override val comment: String? = null
            override val annotations: List<LsiAnnotation> = listOf(createMockDictAnnotation("gender", null, null))
            override val isStatic: Boolean = false
            override val isConstant: Boolean = false
            override val isVar: Boolean = true
            override val isLateInit: Boolean = false
            override val isCollectionType: Boolean = false
            override val defaultValue: String? = null
            override val columnName: String? = null
            override val declaringClass: LsiClass? = null
            override val fieldTypeClass: LsiClass? = null
            override val isNestedObject: Boolean = false
            override val children: List<LsiField> = emptyList()
        }
    }
    
    private fun createMockFieldWithSerializationAlias(): LsiField {
        return object : LsiField {
            override val name: String = "testStr"
            override val type: LsiType? = null
            override val typeName: String = "String"
            override val comment: String? = null
            override val annotations: List<LsiAnnotation> = listOf(
                createMockDictAnnotation(null, "productNameTest1", null)
            )
            override val isStatic: Boolean = false
            override val isConstant: Boolean = false
            override val isVar: Boolean = true
            override val isLateInit: Boolean = false
            override val isCollectionType: Boolean = false
            override val defaultValue: String? = null
            override val columnName: String? = null
            override val declaringClass: LsiClass? = null
            override val fieldTypeClass: LsiClass? = null
            override val isNestedObject: Boolean = false
            override val children: List<LsiField> = emptyList()
        }
    }
    
    private fun createMockFieldWithNameColumn(): LsiField {
        return object : LsiField {
            override val name: String = "testStr2"
            override val type: LsiType? = null
            override val typeName: String = "String"
            override val comment: String? = null
            override val annotations: List<LsiAnnotation> = listOf(
                createMockDictAnnotation(null, null, "test_column")
            )
            override val isStatic: Boolean = false
            override val isConstant: Boolean = false
            override val isVar: Boolean = true
            override val isLateInit: Boolean = false
            override val isCollectionType: Boolean = false
            override val defaultValue: String? = null
            override val columnName: String? = null
            override val declaringClass: LsiClass? = null
            override val fieldTypeClass: LsiClass? = null
            override val isNestedObject: Boolean = false
            override val children: List<LsiField> = emptyList()
        }
    }
    
    private fun createMockDictAnnotation(dicCode: String?, serializationAlias: String?, nameColumn: String?): LsiAnnotation {
        return object : LsiAnnotation {
            override val simpleName: String = "Dict"
            override val qualifiedName: String = "site.addzero.aop.dicttrans.anno.Dict"
            override val attributes: Map<String, Any?> = mapOf(
                "dicCode" to dicCode,
                "serializationAlias" to serializationAlias,
                "nameColumn" to nameColumn
            )
            
            override fun getAttribute(name: String): Any? {
                return when (name) {
                    "dicCode" -> dicCode
                    "serializationAlias" -> serializationAlias
                    "nameColumn" -> nameColumn
                    else -> null
                }
            }
            
            override fun hasAttribute(name: String): Boolean {
                return getAttribute(name) != null
            }
        }
    }
}