package com.addzero.kmp.kaleidoscope.apt

import com.addzero.kmp.kaleidoscope.core.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic


/**
 * @return [Unit]
 */
fun RoundEnvironment.toKldResolver(processingEnv: ProcessingEnvironment): KldResolver {
    val roundEnv = this
    return object : KldResolver {

        override fun getElementsAnnotatedWith(qualifiedName: String): Sequence<KldElement> {
            val typeElement = processingEnv.elementUtils.getTypeElement(qualifiedName)
            return if (typeElement != null) {
                roundEnv.getElementsAnnotatedWith(typeElement).asSequence().map { it.toKldElement() }
            } else {
                emptySequence()
            }
        }

        override fun getElementsAnnotatedWithSimpleName(simpleName: String): Sequence<KldElement> {
            return roundEnv.rootElements.asSequence().flatMap { rootElement ->
                getAllElementsRecursively(rootElement).asSequence().filter { element ->
                    element.annotationMirrors.any { mirror ->
                        val typeElement = mirror.annotationType.asElement() as TypeElement
                        typeElement.simpleName.toString() == simpleName
                    }
                }
            }.map { it.toKldElement() }
        }

        override fun getClassDeclaration(qualifiedName: String): KldTypeElement? {
            return processingEnv.elementUtils.getTypeElement(qualifiedName)?.toKldTypeElement()
        }

        override fun getPackageDeclaration(qualifiedName: String): KldPackageElement? {
            return processingEnv.elementUtils.getPackageElement(qualifiedName)?.toKldPackageElement()
        }

        override fun getAllFiles(): Sequence<KldSourceFile> {
            throw UnsupportedOperationException(
                "APT平台不支持getAllFiles()方法 - APT无法直接访问所有源文件信息。" + "如需此功能，请使用KSP平台或通过其他方式获取文件列表。"
            )
        }

        override fun getOptions(): Map<String, String> {
            return processingEnv.options
        }

        override val isProcessingOver: Boolean = roundEnv.processingOver()

        override val rootElements: Sequence<KldElement>
            get() = roundEnv.rootElements.asSequence().map { it.toKldElement() }

        override fun createSourceFile(
            packageName: String, fileName: String, vararg originatingElements: KldElement
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

        override fun info(message: String, element: KldElement?) {
            val aptElement = element as? Element
            if (aptElement != null) {
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, message, aptElement)
            } else {
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, message)
            }
        }

        override fun warn(message: String, element: KldElement?) {
            val aptElement = element as? Element
            if (aptElement != null) {
                processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, message, aptElement)
            } else {
                processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, message)
            }
        }

        override fun error(message: String, element: KldElement?) {
            val aptElement = element as? Element
            if (aptElement != null) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message, aptElement)
            } else {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
            }
        }

        private fun getAllElementsRecursively(element: Element): List<Element> {
            val result = mutableListOf<Element>()
            result.add(element)
            element.enclosedElements.forEach { enclosedElement ->
                result.addAll(getAllElementsRecursively(enclosedElement))
            }
            return result
        }
    }

}


/**
 * APT 平台的 KldWriter 实现
 */
class AptKldWriter(private val writer: java.io.Writer) : KldWriter {
    override fun write(text: String) {
        writer.write(text)
    }

    override fun close() {
        writer.close()
    }
}

