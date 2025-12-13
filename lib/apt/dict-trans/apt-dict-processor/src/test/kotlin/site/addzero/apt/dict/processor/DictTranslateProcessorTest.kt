package site.addzero.apt.dict.processor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import site.addzero.apt.dict.annotations.DictField
import site.addzero.apt.dict.annotations.DictTranslate

/**
 * Test for DictTranslateProcessor APT functionality
 */
class DictTranslateProcessorTest {
    
    @Test
    fun `test DictFieldInfo creation from annotation`() {
        // Test creating DictFieldInfo from annotation data
        val dictFieldInfo = DictFieldInfo(
            sourceField = "status",
            targetField = "statusText",
            dictCode = "user_status",
            table = "",
            codeColumn = "",
            nameColumn = "",
            spelExp = "",
            condition = ""
        )
        
        assertEquals("status", dictFieldInfo.sourceField)
        assertEquals("statusText", dictFieldInfo.targetField)
        assertEquals("user_status", dictFieldInfo.dictCode)
        assertTrue(dictFieldInfo.table.isEmpty())
    }
    
    @Test
    fun `test table dictionary field info`() {
        val dictFieldInfo = DictFieldInfo(
            sourceField = "departmentId",
            targetField = "departmentText",
            dictCode = "",
            table = "sys_department",
            codeColumn = "id",
            nameColumn = "dept_name",
            spelExp = "",
            condition = "status = 1"
        )
        
        assertEquals("departmentId", dictFieldInfo.sourceField)
        assertEquals("departmentText", dictFieldInfo.targetField)
        assertEquals("sys_department", dictFieldInfo.table)
        assertEquals("id", dictFieldInfo.codeColumn)
        assertEquals("dept_name", dictFieldInfo.nameColumn)
        assertEquals("status = 1", dictFieldInfo.condition)
    }
    
    @Test
    fun `test processor annotation support`() {
        val processor = DictTranslateProcessor()
        
        // Verify supported annotations
        val supportedAnnotations = processor.supportedAnnotationTypes
        assertTrue(supportedAnnotations.contains("site.addzero.apt.dict.annotations.DictTranslate"))
    }
    
    @Test
    fun `test processor source version`() {
        val processor = DictTranslateProcessor()
        
        // Verify supported source version
        val sourceVersion = processor.supportedSourceVersion
        assertNotNull(sourceVersion)
    }
}

/**
 * Example entity for testing APT processing
 */
@DictTranslate(suffix = "Enhanced")
data class TestUserEntity(
    val id: Long,
    
    @DictField(dictCode = "user_status", targetField = "statusText")
    val status: String,
    
    @DictField(
        table = "sys_department", 
        codeColumn = "id", 
        nameColumn = "dept_name", 
        targetField = "departmentText"
    )
    val departmentId: Long,
    
    @DictField(
        table = "sys_region",
        codeColumn = "code",
        nameColumn = "name",
        targetField = "regionText",
        condition = "status = 1 AND level = 2"
    )
    val regionCode: String
)