package site.addzero.ksp.metadata.semantic

/**
 * 辅助工具类：帮助 SPI 实现方构建元数据
 */
object SemanticHelper {

    /**
     * 为枚举类型生成展开映射
     * @param originMethod 原始方法名
     * @param paramName 参数名
     * @param enumValues 枚举项列表 (可以用 Enum.values())
     * @param nameTemplate 方法名模板，如 "{method}{Name}"
     * @param valueProvider 如何从枚举项获取存入参数的值
     */
    fun <E : Enum<E>> expandEnum(
        originMethod: String,
        paramName: String,
        enumValues: Array<E>,
        nameTemplate: String = "{method}{Name}",
        docTemplate: String = "自动生成的{Name}方法",
        valueProvider: (E) -> Any?
    ): List<SemanticMethodDefinition> {
        return enumValues.map { e ->
            val eName = e.name.lowercase().split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
            val newName = nameTemplate.replace("{method}", originMethod).replace("{Name}", eName)
            val doc = docTemplate.replace("{Name}", e.name)
            SemanticMethodDefinition(
                newMethodName = newName,
                fixedParameters = mapOf(paramName to valueProvider(e)),
                doc = doc
            )
        }
    }

    /**
     * 笛卡尔积组合逻辑
     */
    fun combine(
        base: List<SemanticMethodDefinition>,
        other: List<SemanticMethodDefinition>,
        nameTemplate: String = "{name1}And{name2}"
    ): List<SemanticMethodDefinition> {
        return base.flatMap { b ->
            other.map { o ->
                SemanticMethodDefinition(
                    newMethodName = nameTemplate
                        .replace("{name1}", b.newMethodName)
                        .replace("{name2}", o.newMethodName),
                    fixedParameters = b.fixedParameters + o.fixedParameters,
                    doc = "${b.doc ?: ""} & ${o.doc ?: ""}"
                )
            }
        }
    }

    /**
     * 从 CSV 内容加载「语义化对照表」
     * 格式示例：
     * SaveMode,AssociatedSaveMode,NewMethodName,Doc
     * UPSERT,REPLACE,sync,全量同步
     * INSERT_ONLY,APPEND,init,初始化
     *
     * @param csvContent CSV 文本内容
     * @param originMethod 原始方法名
     * @param paramNames 列名到方法形参名的映射，例如 listOf("mode", "associatedMode")
     * @param fqns 枚举类的全限定名前缀，例如 mapOf("mode" to "org.babyfish.jimmer.sql.ast.mutation.SaveMode")
     */
    fun fromCsv(
        csvContent: String,
        originMethod: String,
        paramNames: List<String>,
        fqns: Map<String, String>
    ): List<SemanticMethodDefinition> {
        val lines = csvContent.trim().split("\n")
        if (lines.isEmpty()) return emptyList()
        
        // 假设第一行是 Header，我们从第二行开始
        return lines.drop(1).mapNotNull { line ->
            val columns = line.split(",").map { it.trim() }
            if (columns.size < paramNames.size + 1) return@mapNotNull null
            
            val fixedParams = mutableMapOf<String, Any?>()
            paramNames.forEachIndexed { index, param ->
                val value = columns[index]
                val fqn = fqns[param]
                fixedParams[param] = if (fqn != null) "$fqn.$value" else value
            }
            
            val newName = columns[paramNames.size]
            val doc = columns.getOrNull(paramNames.size + 1)
            
            SemanticMethodDefinition(newName, fixedParams, doc)
        }
    }
}