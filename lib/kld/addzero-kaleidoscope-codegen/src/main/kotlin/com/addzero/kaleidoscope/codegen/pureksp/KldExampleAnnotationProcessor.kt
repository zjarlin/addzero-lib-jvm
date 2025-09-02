package com.addzero.kaleidoscope.codegen.pureksp

import com.addzero.kaleidoscope.KldProcessor
import com.addzero.kaleidoscope.apt.KldAnnotationProcessor
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion

/**
 * KldExampleProcessor 的 APT 提供者
 */
@SupportedAnnotationTypes("*") // 支持所有注解类型
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class KldExampleAnnotationProcessor : KldAnnotationProcessor() {
    override fun createKldProcessor(): KldProcessor {
        return KldExampleProcessor()
    }
}