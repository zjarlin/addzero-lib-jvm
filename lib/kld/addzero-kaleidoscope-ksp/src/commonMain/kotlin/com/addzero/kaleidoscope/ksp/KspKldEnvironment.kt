package com.addzero.kaleidoscope.ksp

import com.addzero.kaleidoscope.KldEnvironment
import com.addzero.kaleidoscope.core.KldLogger
import com.addzero.kaleidoscope.core.KldWriter
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * KSP 平台的 KldEnvironment 实现
 */
class KspKldEnvironment(
    private val environment: SymbolProcessorEnvironment
) : KldEnvironment {

    override val options: Map<String, String>
        get() = environment.options
    override val logger: KldLogger
        get() = object : KldLogger {
            override fun info(message: String, element: Any?) {
                val kspSymbol = element as? KSAnnotated
                if (kspSymbol != null) {
                    environment.logger.info(message, kspSymbol)
                } else {
                    environment.logger.info(message)
                }
            }

            override fun warn(message: String, element: Any?) {
                val kspSymbol = element as? KSAnnotated
                if (kspSymbol != null) {
                    environment.logger.warn(message, kspSymbol)
                } else {
                    environment.logger.warn(message)
                }

            }

            override fun error(message: String, element: Any?) {
                val kspSymbol = element as? KSAnnotated
                if (kspSymbol != null) {
                    environment.logger.error(message, kspSymbol)
                } else {
                    environment.logger.error(message)
                }

            }
        }

    override fun createSourceFile(
        packageName: String,
        fileName: String,
        vararg originatingElements: Any
    ): KldWriter {
        val dependencies = Dependencies(
            false,
            *originatingElements.mapNotNull { element ->
                when (val symbol = (element as? KSAnnotated)) {
                    is KSAnnotated -> symbol.containingFile
                    else -> null
                }
            }.toTypedArray()
        )

        val outputStream = environment.codeGenerator.createNewFile(
            dependencies,
            packageName,
            fileName
        )

        return KspKldWriter(outputStream.writer())
    }

}
