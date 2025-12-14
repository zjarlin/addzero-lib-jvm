package site.addzero.apt.dict.processor.unified

import org.junit.jupiter.api.Test
import site.addzero.apt.dict.processor.DictTranslateProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.JavaFileObject
import kotlin.test.assertNotNull

/**
 * 统一元编程处理器测试
 */
class UnifiedMetaprogrammingProcessorTest {

    @Test
    fun `test processor initialization`() {
        val processor = DictTranslateProcessor()
        assertNotNull(processor)
    }

    @Test
    fun `test LSI abstraction integration`() {
        // 这里可以添加更详细的集成测试
        // 验证APT元素到LSI抽象的转换是否正确
        val processor = DictTranslateProcessor()
        
        // 验证处理器支持的注解类型
        val supportedAnnotations = processor.supportedAnnotationTypes
        assert(supportedAnnotations.contains("site.addzero.aop.dicttrans.anno.Dict"))
    }
}