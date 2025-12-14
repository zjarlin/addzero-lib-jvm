package site.addzero.apt.dict.processor

import site.addzero.apt.dict.processor.unified.UnifiedMetaprogrammingProcessor
import site.addzero.apt.dict.processor.unified.context.MetaprogrammingContext
import site.addzero.apt.dict.processor.unified.context.MetaprogrammingContext.getAnnotatedFields
import site.addzero.apt.dict.processor.unified.generator.UnifiedCodeGenerator
import site.addzero.dict.trans.inter.PrecompiledSql
import site.addzero.dict.trans.inter.TableTranslateContext
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.context.LsiContext
import site.addzero.util.lsi.field.LsiField
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * 基于统一元编程框架的字典翻译处理器
 *
 * 使用LSI抽象体系重构的APT处理器，提供编译时字典翻译功能
 * 相比原版本，具有更好的代码组织、可扩展性和维护性
 */
@SupportedAnnotationTypes("site.addzero.aop.dicttrans.anno.Dict")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class DictTranslateProcessor : UnifiedMetaprogrammingProcessor() {

    private lateinit var codeGenerator: UnifiedCodeGenerator

    override fun onInit(context: MetaprogrammingContext) {
        // 初始化代码生成器
        codeGenerator = UnifiedCodeGenerator()

        // 字典翻译处理器特定的初始化逻辑
        context.reportInfo("DictTranslateProcessor initialized with unified metaprogramming framework")
    }

    override fun processLsiClass(
        originalElement: TypeElement,
        lsiClass: LsiClass,
        lsiContext: LsiContext
    ): Boolean {
        // 使用LSI抽象获取@Dict注解的字段
        val dictFields = lsiClass.getAnnotatedFields("Dict")

        if (dictFields.isEmpty()) {
            return true // 没有@Dict字段，跳过处理
        }

        // 生成字典DSL类
        return generateDictDsl(originalElement, lsiClass, dictFields)
    }

    private fun generateDictDsl(
        originalElement: TypeElement,
        lsiClass: LsiClass,
        dictFields: List<LsiField>
    ): Boolean {
        return try {
            val packageName = lsiClass.qualifiedName?.substringBeforeLast('.') ?: ""
            val originalClassName = lsiClass.name ?: "Unknown"
            val dslClassName = "${originalClassName}DictDsl"

            // 使用LSI抽象收集字典翻译上下文
            val (tableContexts, systemDicts) = collectDictContexts(dictFields)

            // 生成预编译SQL
            val precompiledSqls = generatePrecompiledSqls(tableContexts)

            // 使用统一代码生成器生成Java代码
            val additionalMethods = listOf(
                generateTranslateMethod(systemDicts, precompiledSqls),
                generateSystemDictMethod(systemDicts),
                generateTableDictMethod(precompiledSqls)
            )

            val javaCode = codeGenerator.generateJavaClass(
                packageName,
                dslClassName,
                lsiClass,
                dictFields,
                additionalMethods
            )

            // 写入文件
            metaprogrammingContext.createSourceFile("$packageName.$dslClassName", javaCode)
            true

        } catch (e: Exception) {
            metaprogrammingContext.reportError("Failed to generate DictDsl for ${lsiClass.name}: ${e.message}", e)
            false
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
            public static ${this::class.simpleName}DictDsl translate(Object original, TransApi transApi) {
                ${this::class.simpleName}DictDsl dsl = new ${this::class.simpleName}DictDsl(original, transApi);
                dsl.performTranslation();
                return dsl;
            }

            private void performTranslation() {
                // Concurrent translation using CompletableFuture
                CompletableFuture<Void> systemDictFuture = CompletableFuture.runAsync(() -> translateSystemDict());
                CompletableFuture<Void> tableDictFuture = CompletableFuture.runAsync(() -> translateTableDict());

                CompletableFuture.allOf(systemDictFuture, tableDictFuture).join();
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
                // Set translated values - simplified for now
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
                "" // keys from field
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
        lsiClass: LsiClass,
        maxDepth: Int = 5
    ): List<LsiField> {
        return metaprogrammingContext.getAllFieldsRecursively(lsiClass, maxDepth)
            .filter { field ->
                field.annotations.any { it.simpleName == "Dict" }
            }
    }


}

