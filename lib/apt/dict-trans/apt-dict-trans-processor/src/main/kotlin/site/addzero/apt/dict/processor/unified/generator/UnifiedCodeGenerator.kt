package site.addzero.apt.dict.processor.unified.generator

import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField

/**
 * 统一代码生成器
 *
 * 基于LSI抽象生成多语言代码，支持Java、Kotlin等
 */
class UnifiedCodeGenerator {

    /**
     * 生成Java类代码
     */
    fun generateJavaClass(
        packageName: String,
        className: String,
        originalClass: LsiClass,
        fields: List<LsiField>,
        additionalMethods: List<String> = emptyList()
    ): String {
        val imports = generateImports()
        val classDeclaration = generateClassDeclaration(className, originalClass)
        val fieldDeclarations = generateFieldDeclarations(fields)
        val constructor = generateConstructor(className, originalClass)
        val methods = generateMethods(additionalMethods)

        return """
            package $packageName;

            $imports

            $classDeclaration {
                $fieldDeclarations
                
                $constructor
                
                $methods
            }
        """.trimIndent()
    }

    private fun generateImports(): String {
        return """
            import site.addzero.dict.trans.inter.TransApi;
            import site.addzero.dict.trans.inter.PrecompiledSql;
            import site.addzero.apt.dict.model.DictModel;
            import java.util.*;
            import java.util.concurrent.CompletableFuture;
        """.trimIndent()
    }

    private fun generateClassDeclaration(className: String, originalClass: LsiClass): String {
        val originalClassName = originalClass.name ?: "Object"
        return "public class $className extends $originalClassName"
    }

    private fun generateFieldDeclarations(fields: List<LsiField>): String {
        return fields.joinToString("\n") { field ->
            val fieldType = field.typeName ?: "Object"
            val fieldName = field.name ?: "unknown"
            "    private $fieldType ${fieldName}Translated;"
        }
    }

    private fun generateKotlinProperties(fields: List<LsiField>): String {
        return fields.joinToString("\n") { field ->
            val fieldType = field.typeName ?: "Any"
            val fieldName = field.name ?: "unknown"
            "    var ${fieldName}Translated: $fieldType? = null"
        }
    }

    private fun generateConstructor(className: String, originalClass: LsiClass): String {
        val originalClassName = originalClass.name ?: "Object"
        return """
            private final TransApi transApi;

            public $className($originalClassName original, TransApi transApi) {
                // Copy all fields from original
                this.transApi = transApi;
            }
        """.trimIndent()
    }

    private fun generateMethods(additionalMethods: List<String>): String {
        return additionalMethods.joinToString("\n\n")
    }

}
