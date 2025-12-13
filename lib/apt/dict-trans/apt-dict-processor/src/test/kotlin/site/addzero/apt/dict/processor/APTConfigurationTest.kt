package site.addzero.apt.dict.processor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import javax.annotation.processing.Processor
import java.util.ServiceLoader

/**
 * Test to verify APT processor configuration
 * 
 * This test ensures that the APT processor is correctly registered
 * and can be discovered by the Java annotation processing framework.
 */
class APTConfigurationTest {
    
    @Test
    fun `test APT processor is registered via ServiceLoader`() {
        val processors = ServiceLoader.load(Processor::class.java).toList()
        
        // Check if our processor is registered
        val dictProcessor = processors.find { it is DictTranslateProcessor }
        assertNotNull(dictProcessor, "DictTranslateProcessor should be registered via ServiceLoader")
        
        // Verify processor configuration
        assertTrue(dictProcessor is DictTranslateProcessor)
        
        val processor = dictProcessor as DictTranslateProcessor
        
        // Check supported annotation types
        val supportedAnnotations = processor.supportedAnnotationTypes
        assertTrue(supportedAnnotations.contains("site.addzero.apt.dict.annotations.DictTranslate"))
        
        // Check supported source version
        assertNotNull(processor.supportedSourceVersion)
    }
    
    @Test
    fun `test processor can be instantiated`() {
        assertDoesNotThrow {
            val processor = DictTranslateProcessor()
            assertNotNull(processor)
        }
    }
    
    @Test
    fun `test processor supported annotations`() {
        val processor = DictTranslateProcessor()
        val supportedAnnotations = processor.supportedAnnotationTypes
        
        assertFalse(supportedAnnotations.isEmpty())
        assertTrue(supportedAnnotations.contains("site.addzero.apt.dict.annotations.DictTranslate"))
    }
    
    @Test
    fun `test processor supported source version`() {
        val processor = DictTranslateProcessor()
        val sourceVersion = processor.supportedSourceVersion
        
        assertNotNull(sourceVersion)
        // Should support at least Java 8
        assertTrue(sourceVersion.ordinal >= javax.lang.model.SourceVersion.RELEASE_8.ordinal)
    }
}