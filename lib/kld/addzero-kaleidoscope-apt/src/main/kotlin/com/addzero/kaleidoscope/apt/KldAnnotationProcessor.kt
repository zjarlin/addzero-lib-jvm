package com.addzero.kaleidoscope.apt

import com.addzero.kaleidoscope.KldProcessor
import com.addzero.kaleidoscope.core.KldResolver
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * APT 平台的 KldProcessor 实现
 *
 * 将 KldProcessor 包装为 APT AnnotationProcessor
 */
abstract class KldAnnotationProcessor : AbstractProcessor() {

    /**
     * 创建 KldProcessor 实例
     */
    protected abstract fun createKldProcessor(): KldProcessor

    private var kldProcessor: KldProcessor? = null
    private var kldResolver: KldResolver? = null
    private var kldEnvironment: AptKldEnvironment? = null

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        kldProcessor = createKldProcessor()
        kldEnvironment = AptKldEnvironment(processingEnv)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        kldProcessor?.let { processor ->
            kldEnvironment?.let { environment ->
                // 创建 KldResolver 实例
                kldResolver = roundEnv.toKldResolver(processingEnv)
                
                // 调用 KldProcessor 的 process 方法
                val shouldContinue = processor.process(kldResolver!!, environment)
                
                // 如果是最后一轮处理，调用 finish 方法
                if (roundEnv.processingOver()) {
                    processor.finish(kldResolver!!, environment)
                }
                
                return shouldContinue
            }
        }
        
        return false
    }
}