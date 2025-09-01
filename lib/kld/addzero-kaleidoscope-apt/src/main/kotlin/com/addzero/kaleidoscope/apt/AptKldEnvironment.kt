package com.addzero.kaleidoscope.apt

import com.addzero.kaleidoscope.KldEnvironment
import com.addzero.kaleidoscope.core.KldWriter
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

/**
 * APT 平台的 KldEnvironment 实现
 */
class AptKldEnvironment(
    private val processingEnv: ProcessingEnvironment
) : KldEnvironment {

    override val options: Map<String, String>
        get() = processingEnv.options

    override fun createSourceFile(
        packageName: String,
        fileName: String,
        vararg originatingElements: Any
    ): KldWriter {
        val qualifiedName = if (packageName.isNotEmpty()) {
            "$packageName.$fileName"
        } else {
            fileName
        }

        val aptElements = originatingElements.mapNotNull { element ->
            when (element) {
                is Element -> element
                else -> null
            }
        }.toTypedArray()

        val javaFileObject = processingEnv.filer.createSourceFile(qualifiedName, *aptElements)
        return AptKldWriter(javaFileObject.openWriter())
    }

    override fun info(message: String, element: Any?) {
        val aptElement = element as? Element
        if (aptElement != null) {
            processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, message, aptElement)
        } else {
            processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, message)
        }
    }

    override fun warn(message: String, element: Any?) {
        val aptElement = element as? Element
        if (aptElement != null) {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, message, aptElement)
        } else {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, message)
        }
    }

    override fun error(message: String, element: Any?) {
        val aptElement = element as? Element
        if (aptElement != null) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message, aptElement)
        } else {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
        }
    }
}