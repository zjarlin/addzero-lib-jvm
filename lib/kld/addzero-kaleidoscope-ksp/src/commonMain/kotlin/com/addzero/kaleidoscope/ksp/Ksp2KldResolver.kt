package com.addzero.kaleidoscope.ksp

import com.addzero.kaleidoscope.core.KldElement
import com.addzero.kaleidoscope.core.KldPackageElement
import com.addzero.kaleidoscope.core.KldResolver
import com.addzero.kaleidoscope.core.KldSourceFile
import com.addzero.kaleidoscope.core.KldTypeElement
import com.addzero.kaleidoscope.core.KldWriter
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration

fun Resolver.toKldResolver(environment: SymbolProcessorEnvironment): KldResolver {
    val resolver = this
    return object : KldResolver {

    override fun getElementsAnnotatedWith(qualifiedName: String): Sequence<KldElement> {
        return resolver.getSymbolsWithAnnotation(qualifiedName)
            .map { it.toKldElement() }
    }

    override fun getElementsAnnotatedWithSimpleName(simpleName: String): Sequence<KldElement> {
        return resolver.getAllFiles().flatMap { file ->
            file.declarations.filter { declaration ->
                declaration.annotations.any { annotation ->
                    annotation.annotationType.resolve().declaration.simpleName.asString() == simpleName
                }
            }
        }.map { it.toKldElement() }
    }

    override fun getClassDeclaration(qualifiedName: String): KldTypeElement? {
        return resolver.getClassDeclarationByName(resolver.getKSNameFromString(qualifiedName))?.toKldTypeElement()
    }

    override fun getPackageDeclaration(qualifiedName: String): KldPackageElement? {
        throw UnsupportedOperationException(
            "KSP平台不支持getPackageDeclaration()方法 - KSP中没有独立的包元素概念。" +
            "如需包信息，请通过KSDeclaration.packageName属性获取。"
        )
    }

    override fun getAllFiles(): Sequence<KldSourceFile> {
        return resolver.getAllFiles().map { it.toKldSourceFile() }
    }

    override fun getOptions(): Map<String, String> {
        return environment.options
    }

    override val isProcessingOver = false // KSP没有明确的"处理结束"概念

    override val rootElements
        get() = resolver.getAllFiles().flatMap { file ->
            file.declarations.map { it.toKldElement() }
        }

    override fun createSourceFile(
        packageName: String,
        fileName: String,
        vararg originatingElements: KldElement
    ): KldWriter {
        val dependencies = Dependencies(
            false,
            *originatingElements.mapNotNull { element ->
                when (val symbol = (element as? KSAnnotated)) {
                    is KSDeclaration -> symbol.containingFile
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

    override fun info(message: String, element: KldElement?) {
        val kspSymbol = element as? KSAnnotated
        if (kspSymbol != null) {
            environment.logger.info(message, kspSymbol)
        } else {
            environment.logger.info(message)
        }
    }

    override fun warn(message: String, element: KldElement?) {
        val kspSymbol = element as? KSAnnotated
        if (kspSymbol != null) {
            environment.logger.warn(message, kspSymbol)
        } else {
            environment.logger.warn(message)
        }
    }

    override fun error(message: String, element: KldElement?) {
        val kspSymbol = element as? KSAnnotated
        if (kspSymbol != null) {
            environment.logger.error(message, kspSymbol)
        } else {
            environment.logger.error(message)
        }
    }
}


}
