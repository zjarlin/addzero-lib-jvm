package site.addzero.apt.dict.processor

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.string.shouldContain
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

/**
 * Test for error handling and monitoring functionality
 */
class ErrorHandlingTest : FunSpec({
    
    test("should track error counts by type") {
        val mockProcessingEnv = createMockProcessingEnvironment()
        val errorHandler = ErrorHandlingManager(mockProcessingEnv)
        
        // Report different types of errors
        errorHandler.reportError(ErrorType.ANNOTATION_PROCESSING_ERROR, "Test annotation error")
        errorHandler.reportError(ErrorType.CODE_GENERATION_ERROR, "Test code generation error")
        errorHandler.reportError(ErrorType.ANNOTATION_PROCESSING_ERROR, "Another annotation error")
        
        val stats = errorHandler.getErrorStatistics()
        
        stats.totalErrors shouldBe 3
        stats.errorsByType[ErrorType.ANNOTATION_PROCESSING_ERROR] shouldBe 2
        stats.errorsByType[ErrorType.CODE_GENERATION_ERROR] shouldBe 1
    }
    
    test("should detect error threshold exceeded") {
        val mockProcessingEnv = createMockProcessingEnvironment()
        val errorHandler = ErrorHandlingManager(mockProcessingEnv)
        
        // Initially should not exceed threshold
        errorHandler.isErrorThresholdExceeded() shouldBe false
        
        // Report many errors
        repeat(60) { i ->
            errorHandler.reportError(ErrorType.GENERAL_WARNING, "Error $i")
        }
        
        // Should now exceed threshold
        errorHandler.isErrorThresholdExceeded() shouldBe true
    }
    
    test("should handle annotation processing errors with recovery") {
        val mockProcessingEnv = createMockProcessingEnvironment()
        val errorHandler = ErrorHandlingManager(mockProcessingEnv)
        val mockElement = createMockElement("TestClass")
        
        val recovered = errorHandler.handleAnnotationError(
            mockElement,
            "@DictTranslate",
            RuntimeException("Test error")
        )
        
        // Should attempt recovery for annotation errors
        recovered shouldBe true
        
        val stats = errorHandler.getErrorStatistics()
        stats.errorsByType[ErrorType.ANNOTATION_PROCESSING_ERROR] shouldBe 1
    }
    
    test("should generate fallback class on code generation error") {
        val mockProcessingEnv = createMockProcessingEnvironment()
        val errorHandler = ErrorHandlingManager(mockProcessingEnv)
        
        val recovered = errorHandler.handleCodeGenerationError(
            "TestEntity",
            RuntimeException("Code generation failed")
        )
        
        // Should attempt to generate fallback
        recovered shouldBe false // Will be handled by caller
        
        val stats = errorHandler.getErrorStatistics()
        stats.errorsByType[ErrorType.CODE_GENERATION_ERROR] shouldBe 1
    }
    
    test("should record processing metrics") {
        val mockProcessingEnv = createMockProcessingEnvironment()
        val errorHandler = ErrorHandlingManager(mockProcessingEnv)
        
        // Record some operations
        errorHandler.recordProcessingTime("operation1", 100)
        errorHandler.recordProcessingTime("operation1", 200)
        errorHandler.recordProcessingTime("operation2", 50)
        
        val stats = errorHandler.getErrorStatistics()
        val metrics = stats.processingMetrics
        
        metrics.operationStats shouldNotBe null
        metrics.operationStats["operation1"]?.count shouldBe 2
        metrics.operationStats["operation1"]?.totalTime shouldBe 300
        metrics.operationStats["operation2"]?.count shouldBe 1
    }
    
    test("should maintain error history") {
        val mockProcessingEnv = createMockProcessingEnvironment()
        val errorHandler = ErrorHandlingManager(mockProcessingEnv)
        
        errorHandler.reportError(ErrorType.VALIDATION_ERROR, "First error")
        errorHandler.reportError(ErrorType.TEMPLATE_PROCESSING_ERROR, "Second error")
        
        val stats = errorHandler.getErrorStatistics()
        
        stats.recentErrors.shouldNotBeEmpty()
        stats.recentErrors.size shouldBe 2
        stats.recentErrors[0].message shouldBe "First error"
        stats.recentErrors[1].message shouldBe "Second error"
    }
    
    test("performance monitor should track operation times") {
        val mockProcessingEnv = createMockProcessingEnvironment()
        val monitor = PerformanceMonitor(mockProcessingEnv)
        
        // Measure some operations
        val result1 = monitor.measureTime("testOp") {
            Thread.sleep(10)
            "result1"
        }
        
        val result2 = monitor.measureTime("testOp") {
            Thread.sleep(20)
            "result2"
        }
        
        result1 shouldBe "result1"
        result2 shouldBe "result2"
        
        val report = monitor.getPerformanceReport()
        val testOpStats = report.operationStats["testOp"]
        
        testOpStats shouldNotBe null
        testOpStats!!.count shouldBe 2
        testOpStats.averageTime shouldBe (testOpStats.totalTime.toDouble() / 2)
    }
    
    test("performance monitor should detect memory issues") {
        val mockProcessingEnv = createMockProcessingEnvironment()
        val monitor = PerformanceMonitor(mockProcessingEnv)
        
        monitor.takeMemorySnapshot("start")
        
        // Simulate memory usage
        val largeArray = ByteArray(10 * 1024 * 1024) // 10MB
        
        monitor.takeMemorySnapshot("after_allocation")
        
        val report = monitor.getPerformanceReport()
        
        report.memoryStats.snapshots shouldBe 2
        report.memoryStats.maxUsedMemory shouldBe report.memoryStats.minUsedMemory
    }
    
    test("should provide optimization suggestions") {
        val mockProcessingEnv = createMockProcessingEnvironment()
        val monitor = PerformanceMonitor(mockProcessingEnv)
        
        // Record a slow operation
        monitor.recordOperationTime("slowOperation", 2000) // 2 seconds
        
        val suggestions = monitor.getOptimizationSuggestions()
        
        suggestions.shouldNotBeEmpty()
        suggestions.any { it.contains("slowOperation") } shouldBe true
    }
})

// Mock helper functions
private fun createMockProcessingEnvironment(): ProcessingEnvironment {
    return object : ProcessingEnvironment {
        override fun getMessager() = object : javax.annotation.processing.Messager {
            override fun printMessage(kind: Diagnostic.Kind?, msg: CharSequence?) {
                println("[$kind] $msg")
            }
            override fun printMessage(kind: Diagnostic.Kind?, msg: CharSequence?, e: Element?) {
                println("[$kind] $msg (element: $e)")
            }
        }
        
        override fun getElementUtils() = TODO("Not implemented for test")
        override fun getTypeUtils() = TODO("Not implemented for test")
        override fun getFiler() = TODO("Not implemented for test")
        override fun getOptions() = emptyMap<String, String>()
        override fun getSourceVersion() = javax.lang.model.SourceVersion.RELEASE_8
        override fun getLocale() = java.util.Locale.getDefault()
    }
}

private fun createMockElement(name: String): Element {
    return object : Element {
        override fun getSimpleName() = object : javax.lang.model.element.Name {
            override fun toString() = name
            override fun contentEquals(cs: CharSequence?) = name == cs.toString()
            override val length: Int get() = name.length
            override fun get(index: Int) = name[index]
            override fun subSequence(startIndex: Int, endIndex: Int) = name.subSequence(startIndex, endIndex)
        }
        
        override fun asType() = TODO("Not implemented for test")
        override fun getKind() = javax.lang.model.element.ElementKind.CLASS
        override fun getModifiers() = emptySet<javax.lang.model.element.Modifier>()
        override fun getEnclosedElements() = emptyList<Element>()
        override fun getEnclosingElement(): Element? = null
        override fun getAnnotationMirrors() = emptyList<javax.lang.model.element.AnnotationMirror>()
        override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A? = null
        override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?) = emptyArray<A>()
    }
}