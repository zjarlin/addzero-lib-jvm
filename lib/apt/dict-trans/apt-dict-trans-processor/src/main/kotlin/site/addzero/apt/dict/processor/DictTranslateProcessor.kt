package site.addzero.apt.dict.processor

import site.addzero.aop.dicttrans.anno.Dict
import site.addzero.apt.dict.processor.generator.DictCodeGenerator
import site.addzero.dict.trans.inter.PrecompiledSql
import site.addzero.dict.trans.inter.TableTranslateContext
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.logger.LsiLogger
import site.addzero.util.lsi_impl.impl.apt.clazz.toLsiClass
import site.addzero.util.lsi_impl.impl.apt.logger.toLsiLogger
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

/**
 * 基于LSI-APT的字典翻译处理器
 *
 * 使用LSI抽象体系的APT处理器，提供编译时字典翻译功能
 * 直接使用lsi-apt进行代码结构解析和处理
 */
@SupportedAnnotationTypes("site.addzero.aop.dicttrans.anno.Dict")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class DictTranslateProcessor : AbstractProcessor() {

    private lateinit var filer: Filer
    private lateinit var messager: Messager
    private lateinit var elementUtils: Elements
    private lateinit var codeGenerator: DictCodeGenerator
    private lateinit var lsiLogger: LsiLogger


    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        messager = processingEnv.messager
        elementUtils = processingEnv.elementUtils
        codeGenerator = DictCodeGenerator
        lsiLogger = messager.toLsiLogger()
        lsiLogger.info("DictTranslateProcessor initialized with LSI-APT framework")
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {

        try {
            val annotatedElements = roundEnv.getElementsAnnotatedWith(
                Dict::class.java
            )

            // 按类分组处理
            val classesByElement = annotatedElements.mapNotNull { element ->
                    val enclosingClass = getEnclosingClass(element)
                    if (enclosingClass != null) enclosingClass to element else null
                }.groupBy({ it.first }, { it.second })

            for ((typeElement, elements) in classesByElement) {
                processClass(typeElement)
            }

            return true
        } catch (e: Exception) {
            lsiLogger.error("Error processing annotations: ${e.message}")
            return false
        }
    }

    private fun processClass(typeElement: TypeElement) {

        try {
            // 使用LSI-APT转换TypeElement为LsiClass
            val lsiClass = typeElement.toLsiClass(elementUtils)

            // 获取带有@Dict注解的字段
            val dictFields = getDictFields(lsiClass)

            if (dictFields.isEmpty()) {
                return // 没有@Dict字段，跳过处理
            }

            lsiLogger.info("Processing class ${lsiClass.name} with ${dictFields.size} dict fields")

            // 生成字典DSL类
            generateDictDsl(lsiClass, dictFields)

        } catch (e: Exception) {
            lsiLogger.error("Failed to process class ${typeElement.simpleName}: ${e.message}")
        }
    }

    private fun getDictFields(lsiClass: LsiClass): List<LsiField> {
        return lsiClass.fields.filter { field ->
            field.annotations.any { annotation ->
                annotation.simpleName == "Dict" || annotation.qualifiedName == "site.addzero.aop.dicttrans.anno.Dict"
            }
        }
    }

    private fun getEnclosingClass(element: Element): TypeElement? {
        var current = element
        while (current != null) {
            if (current is TypeElement) {
                return current
            }
            current = current.enclosingElement
        }
        return null
    }

    private fun generateDictDsl(lsiClass: LsiClass, dictFields: List<LsiField>) {

        try {
            val packageName = lsiClass.qualifiedName?.substringBeforeLast('.') ?: ""
            val originalClassName = lsiClass.name ?: "Unknown"
            val dslClassName = "${originalClassName}DictDsl"

            // 使用LSI抽象收集字典翻译上下文
            val (tableContexts, systemDicts) = collectDictContexts(dictFields)

            // 生成预编译SQL
            val precompiledSqls = generatePrecompiledSqls(tableContexts)

            // 使用代码生成器生成Java代码
            val javaCode = codeGenerator.generateDictDslClass(
                packageName, dslClassName, lsiClass, dictFields, systemDicts, precompiledSqls
            )

            // 写入文件
            val sourceFile = filer.createSourceFile("$packageName.$dslClassName")
            sourceFile.openWriter().use { writer ->
                writer.write(javaCode)
            }

            lsiLogger.info("Generated DictDsl class: $packageName.$dslClassName")

        } catch (e: Exception) {
            lsiLogger.error("Failed to generate DictDsl for ${lsiClass.name}: ${e.message}")
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate DictDsl: ${e.message}")
        }
    }

    private fun generateJavaClass(
        packageName: String,
        className: String,
        lsiClass: LsiClass,
        dictFields: List<LsiField>,
        systemDicts: Set<String>,
        precompiledSqls: List<PrecompiledSql>
    ): String {
        return """
package $packageName;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import site.addzero.dict.trans.inter.PrecompiledSql;
import site.addzero.dict.trans.inter.TableTranslateContext;
import site.addzero.aop.dicttrans.api.TransApi;
import site.addzero.aop.dicttrans.model.DictModel;

/**
 * Generated dictionary DSL class for ${lsiClass.name}
 * Auto-generated by DictTranslateProcessor using LSI-APT
 */
public class $className {
    private final Object original;
    private final TransApi transApi;
    
    public $className(Object original, TransApi transApi) {
        this.original = original;
        this.transApi = transApi;
    }
    
    ${generateTranslateMethod(systemDicts, precompiledSqls)}
    
    ${generateSystemDictMethod(systemDicts)}
    
    ${generateTableDictMethod(precompiledSqls)}
    
    ${generateFieldAccessors(dictFields)}
}
        """.trimIndent()
    }

    private fun generateFieldAccessors(dictFields: List<LsiField>): String {
        return dictFields.joinToString("\n\n") { field ->
            val fieldName = field.name ?: "unknown"
            val capitalizedName = fieldName.replaceFirstChar { it.uppercase() }
            """
    // Accessor for field: $fieldName
    public Object get$capitalizedName() {
        // Implementation would use reflection or generated getters
        return null; // Placeholder
    }
    
    public void set$capitalizedName(Object value) {
        // Implementation would use reflection or generated setters
        // Placeholder
    }
            """.trimIndent()
        }
    }

    private fun generatePrecompiledSqls(contexts: List<TableTranslateContext>): List<PrecompiledSql> {
        return contexts.map { context ->
            val whereClause = if (context.whereCondition.isNotEmpty()) " AND ${context.whereCondition}" else ""
            val sql =
                "SELECT ${context.codeColumn}, ${context.textColumn} FROM ${context.table} WHERE ${context.codeColumn} IN (?) $whereClause"
            PrecompiledSql(sql, context.table, context.textColumn, context.codeColumn, context.whereCondition)
        }
    }

    /**
     * 使用LSI抽象收集字典翻译上下文
     */
    private fun collectDictContexts(dictFields: List<LsiField>): Pair<List<TableTranslateContext>, Set<String>> {
        val tableContexts = mutableListOf<TableTranslateContext>()
        val systemDicts = mutableSetOf<String>()

        for (field in dictFields) {
            // 从LSI注解中提取@Dict信息
            val dictAnnotation = field.annotations.find { it.simpleName == "Dict" }
            if (dictAnnotation != null) {
                val dicCode = run {
                    val attribute = dictAnnotation.getAttribute("dicCode")
                    attribute as? String
                } ?: ""
                val tab = run {
                    val attribute1 = dictAnnotation.getAttribute("tab")
                    attribute1 as? String
                } ?: ""
                val nameColumn = run {
                    val attribute1 = dictAnnotation.getAttribute("nameColumn")
                    attribute1 as? String
                } ?: ""
                val codeColumn = run {
                    val attribute1 = dictAnnotation.getAttribute("codeColumn")
                    attribute1 as? String
                } ?: ""
                val whereCondition = run {
                    val attribute1 = dictAnnotation.getAttribute("whereCondition")
                    attribute1 as? String
                } ?: ""

                if (dicCode.isNotEmpty()) {
                    systemDicts.add(dicCode)
                } else if (tab.isNotEmpty()) {
                    tableContexts.add(
                        TableTranslateContext(
                            table = tab,
                            textColumn = nameColumn,
                            codeColumn = codeColumn,
                            keys = "", // Will be filled at runtime
                            whereCondition = whereCondition
                        )
                    )
                }
            }
        }

        return Pair(tableContexts, systemDicts)
    }

    /**
     * 生成翻译方法
     */
    private fun generateTranslateMethod(systemDicts: Set<String>, precompiledSqls: List<PrecompiledSql>): String {
        return """
    public static Object translate(Object original, TransApi transApi) {
        // Create DSL instance and perform translation
        Object result = original; // Placeholder - would create actual DSL instance
        
        // Concurrent translation using CompletableFuture
        CompletableFuture<Void> systemDictFuture = CompletableFuture.runAsync(() -> {
            // translateSystemDict logic here
        });
        CompletableFuture<Void> tableDictFuture = CompletableFuture.runAsync(() -> {
            // translateTableDict logic here  
        });

        CompletableFuture.allOf(systemDictFuture, tableDictFuture).join();
        return result;
    }
        """.trimIndent()
    }

    /**
     * 生成系统字典翻译方法
     */
    private fun generateSystemDictMethod(systemDicts: Set<String>): String {
        if (systemDicts.isEmpty()) {
            return """
    private void translateSystemDict() {
        // No system dicts to translate
    }
            """.trimIndent()
        }

        val dictCodes = systemDicts.joinToString(",")
        return """
    private void translateSystemDict() {
        String dictCodes = "$dictCodes";
        // Collect keys from annotated fields
        List<DictModel> dictModels = transApi.translateDictBatchCode2name(dictCodes, null);
        // Set translated values - implementation would be generated based on specific fields
    }
        """.trimIndent()
    }

    /**
     * 生成表字典翻译方法
     */
    private fun generateTableDictMethod(precompiledSqls: List<PrecompiledSql>): String {
        if (precompiledSqls.isEmpty()) {
            return """
    private void translateTableDict() {
        // No table dicts to translate
    }
            """.trimIndent()
        }

        val sqlExecutions = precompiledSqls.joinToString("\n        ") { sql ->
            """
        List<Map<String, Object>> results${sql.table} = transApi.executePrecompiledTableSql(
            new PrecompiledSql("${sql.sqlTemplate}", "${sql.table}", "${sql.textColumn}", "${sql.codeColumn}", "${sql.whereCondition}"),
            "" // keys from field - would be extracted from actual field values
        );
        // Set translated values for ${sql.table}
            """.trimIndent()
        }

        return """
    private void translateTableDict() {
        $sqlExecutions
    }
        """.trimIndent()
    }

    /**
     * 递归提取字典字段，使用LSI抽象自动检测嵌套结构
     */
    private fun extractDictFieldsRecursively(
        lsiClass: LsiClass, maxDepth: Int = 5, currentDepth: Int = 0
    ): List<LsiField> {
        if (currentDepth >= maxDepth) {
            return emptyList()
        }

        val directFields = lsiClass.fields.filter { field ->
            field.annotations.any { it.simpleName == "Dict" }
        }

        val nestedFields =
            lsiClass.fields.filter { it.isNestedObject }.mapNotNull { it.fieldTypeClass }.flatMap { nestedClass ->
                    extractDictFieldsRecursively(nestedClass, maxDepth, currentDepth + 1)
                }

        return directFields + nestedFields
    }
}

