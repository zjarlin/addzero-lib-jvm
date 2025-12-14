package site.addzero.apt.dict.processor

import site.addzero.util.lsi.field.LsiField
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * APT processor for compile-time dictionary translation
 *
 * This processor generates enhanced entity classes at compile time using APT (Annotation Processing Tool)
 * instead of runtime reflection, providing better performance and type safety.
 *
 * The processor now generates pure Java code using JavaEntityEnhancer with Kotlin multiline strings.
 */
@SupportedAnnotationTypes("site.addzero.aop.dicttrans.anno.Dict")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class DictTranslateProcessor : AbstractProcessor() {

    override fun init(processingEnv: ProcessingEnvironment) {
        val options = processingEnv.options
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv == null) return false

        return true
    }


    /**
     * Writes the generated Java code to a file
     */
    private fun writeJavaFile(packageName: String, className: String, javaCode: String) {
        try {
            val sourceFile = processingEnv.filer.createSourceFile("$packageName.$className")
            sourceFile.openWriter().use { writer ->
                writer.write(javaCode)
            }
        } catch (e: IOException) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Failed to write Java file: ${e.message}"
            )
        }
    }

    private fun extractDictFields(typeElement: TypeElement): List<LsiField> {

        // 获取所有字段，包括嵌套字段
        return emptyList()
    }

    /**
     * 递归提取字典字段，自动检测嵌套结构和 List
     */
    private fun extractDictFieldsRecursively(
        typeElement: TypeElement,
        dictFields: MutableList<LsiField>,
        fieldPrefix: String,
        depth: Int = 0
    ) {
        // 防止无限递归
    }


}

