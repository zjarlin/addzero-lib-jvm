package site.addzero.apt.dict.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DictTranslateProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DictTranslateProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}