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
}
