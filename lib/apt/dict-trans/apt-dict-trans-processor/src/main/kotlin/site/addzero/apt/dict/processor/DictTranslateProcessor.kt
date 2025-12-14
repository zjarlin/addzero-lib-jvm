package site.addzero.apt.dict.processor

import site.addzero.aop.dicttrans.anno.Dict
import site.addzero.dict.trans.inter.PrecompiledSql
import site.addzero.dict.trans.inter.TableTranslateContext
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi_impl.impl.apt.field.AptLsiField
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
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
        super.init(processingEnv)
        val options = processingEnv.options
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv == null) return false

        // Find all elements annotated with @Dict
        val dictAnnotatedElements = roundEnv.getElementsAnnotatedWith(Dict::class.java)

        dictAnnotatedElements
            .filter {
                it.kind == ElementKind.FIELD && it is VariableElement
                AptLsiField( it)
            }
            .associateBy {
                val enclosingClass = it.enclosingElement as? TypeElement
                enclosingClass
            }
        // Group by enclosing class
        val classToFields = mutableMapOf<TypeElement, MutableList<VariableElement>>()

        for (element in dictAnnotatedElements) {
            if (element.kind == ElementKind.FIELD && element is VariableElement) {
                val enclosingClass = element.enclosingElement as? TypeElement ?: continue
                classToFields.computeIfAbsent(enclosingClass) { mutableListOf() }.add(element)
            }
        }

        // Generate DictDsl for each class
        for ((originalClass, fields) in classToFields) {
            generateDictDsl(originalClass, fields)
        }

        return true
    }

    private fun generateDictDsl(originalClass: TypeElement, dictFields: List<VariableElement>) {
        val packageName = processingEnv.elementUtils.getPackageOf(originalClass).qualifiedName.toString()
        val originalClassName = originalClass.simpleName.toString()
        val dslClassName = "${originalClassName}DictDsl"

        // Collect table translate contexts
        val tableContexts = mutableListOf<TableTranslateContext>()
        val systemDicts = mutableSetOf<String>()

        for (field in dictFields) {
            val dictAnno = field.getAnnotation(Dict::class.java)
            if (dictAnno.dicCode.isNotEmpty()) {
                systemDicts.add(dictAnno.dicCode)
            } else if (dictAnno.tab.isNotEmpty()) {
                tableContexts.add(
                    TableTranslateContext(
                        table = dictAnno.tab,
                        textColumn = dictAnno.nameColumn,
                        codeColumn = dictAnno.codeColumn,
                        keys = "", // Will be filled at runtime
                        whereCondition = dictAnno.whereCondition
                    )
                )
            }
        }

        // Generate precompiled SQL for table dicts
        val precompiledSqls = generatePrecompiledSqls(tableContexts)

        // Generate Java code
        val javaCode =
            generateJavaCode(packageName, originalClassName, dslClassName, dictFields, systemDicts, precompiledSqls)

        writeJavaFile(packageName, dslClassName, javaCode)
    }

    private fun generatePrecompiledSqls(contexts: List<TableTranslateContext>): List<PrecompiledSql> {
        return contexts.map { context ->
            val whereClause = if (context.whereCondition.isNotEmpty()) " AND ${context.whereCondition}" else ""
            val sql =
                "SELECT ${context.codeColumn}, ${context.textColumn} FROM ${context.table} WHERE ${context.codeColumn} IN (?) $whereClause"
            PrecompiledSql(sql, context.table, context.textColumn, context.codeColumn, context.whereCondition)
        }
    }

    private fun generateJavaCode(
        packageName: String,
        originalClassName: String,
        dslClassName: String,
        dictFields: List<VariableElement>,
        systemDicts: Set<String>,
        precompiledSqls: List<PrecompiledSql>
    ): String {
        // Use template similar to test class
        return """
            package $packageName;

            import site.addzero.dict.trans.inter.TransApi;
            import site.addzero.dict.trans.inter.PrecompiledSql;
            import site.addzero.apt.dict.model.DictModel;
            import java.util.*;
            import java.util.concurrent.CompletableFuture;

            public class $dslClassName extends $originalClassName {
                private final TransApi transApi;

                public $dslClassName($originalClassName original, TransApi transApi) {
                    // Copy all fields from original - simplified, in practice use reflection or manual copy
                    this.transApi = transApi;
                }

                public static $dslClassName translate($originalClassName user, TransApi transApi) {
                    $dslClassName dsl = new $dslClassName(user, transApi);
                    dsl.performTranslation();
                    return dsl;
                }

                private void performTranslation() {
                    // Concurrent translation
                    CompletableFuture<Void> systemDictFuture = CompletableFuture.runAsync(() -> translateSystemDict());
                    CompletableFuture<Void> tableDictFuture = CompletableFuture.runAsync(() -> translateTableDict());

                    CompletableFuture.allOf(systemDictFuture, tableDictFuture).join();
                }

                private void translateSystemDict() {
                    // System dict translation
                    ${generateSystemDictCode(systemDicts)}
                }

                private void translateTableDict() {
                    // Table dict translation
                    ${generateTableDictCode(precompiledSqls)}
                }

                // Additional translated fields would be added here
            }
        """.trimIndent()
    }

    private fun generateSystemDictCode(systemDicts: Set<String>): String {
        if (systemDicts.isEmpty()) return "// No system dicts"

        val dictCodes = systemDicts.joinToString(",")
        return """
            String dictCodes = "$dictCodes";
            // Collect keys from annotated fields
            List<DictModel> dictModels = transApi.translateDictBatchCode2name(dictCodes, null);
            // Set translated values - simplified
        """.trimIndent()
    }

    private fun generateTableDictCode(precompiledSqls: List<PrecompiledSql>): String {
        if (precompiledSqls.isEmpty()) return "// No table dicts"

        return precompiledSqls.joinToString("\n") { sql ->
            """
            List<Map<String, Object>> results${sql.table} = transApi.executePrecompiledTableSql(
                new PrecompiledSql("${sql.sqlTemplate}", "${sql.table}", "${sql.textColumn}", "${sql.codeColumn}", "${sql.whereCondition}"),
                "" // keys from field
            );
            // Set translated values
            """.trimIndent()
        }
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

