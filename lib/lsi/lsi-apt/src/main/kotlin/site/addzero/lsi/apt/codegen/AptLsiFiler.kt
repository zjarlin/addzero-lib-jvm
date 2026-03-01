package site.addzero.lsi.apt.codegen

import site.addzero.lsi.codegen.LsiFiler
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

class AptLsiFiler(val processingEnv: ProcessingEnvironment) : LsiFiler {

    override fun createSourceFile(qualifiedName: String, content: String) {
        try {
            val filer = processingEnv.filer
            val sourceFile = filer.createSourceFile(qualifiedName)
            sourceFile.openWriter().use { writer ->
                writer.write(content)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = "Failed to create source file " + "$qualifiedName: ${e.message}"
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg)
        }
    }
}
