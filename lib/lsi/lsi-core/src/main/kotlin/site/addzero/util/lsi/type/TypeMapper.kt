package site.addzero.util.lsi.type

import site.addzero.util.lsi.assist.TypeChecker

/**
 * 类型映射器 - 提供类型属性的查询功能
 * 
 * 使用策略模式，支持扩展自定义类型规则
 */
object TypeMapper {
    
    /**
     * 类型属性策略
     * @param predicate 类型判断函数
     * @param defaultLength 默认长度 (-1 表示不适用)
     * @param defaultPrecision 默认精度 (用于 DECIMAL，-1 表示不适用)
     * @param defaultScale 默认小数位数 (用于 DECIMAL，-1 表示不适用)
     */
    private data class TypePropertyStrategy(
        val predicate: (javaType: String, fieldName: String) -> Boolean,
        val defaultLength: Int = -1,
        val defaultPrecision: Int = -1,
        val defaultScale: Int = -1
    )
    
    /**
     * 类型属性策略列表（按优先级排序）
     */
    private val TYPE_PROPERTY_STRATEGIES = listOf(
        // 长文本类型 (TEXT) - 优先级最高
        TypePropertyStrategy(
            predicate = { javaType, fieldName -> TypeChecker.isTextType(javaType, fieldName) },
            defaultLength = -1
        ),
        // BigDecimal 类型
        TypePropertyStrategy(
            predicate = { javaType, _ -> TypeChecker.isBigDecimalType(javaType) },
            defaultLength = -1,
            defaultPrecision = 10,
            defaultScale = 2
        ),
        // 字符串类型
        TypePropertyStrategy(
            predicate = { javaType, _ -> TypeChecker.isStringType(javaType) },
            defaultLength = 255
        ),
        // 其他类型（默认）
        TypePropertyStrategy(
            predicate = { _, _ -> true },
            defaultLength = -1
        )
    )
    
    /**
     * 获取默认长度
     * @param javaType Java 类型全限定名或简单名
     * @param fieldName 字段名（用于特殊判断，如 TEXT 类型）
     * @return 默认长度，-1 表示不适用
     */
    fun getDefaultLength(javaType: String, fieldName: String = ""): Int {
        return TYPE_PROPERTY_STRATEGIES
            .first { it.predicate(javaType, fieldName) }
            .defaultLength
    }
    
    /**
     * 获取默认精度（用于 DECIMAL）
     * @param javaType Java 类型全限定名或简单名
     * @return 默认精度，-1 表示不适用
     */
    fun getDefaultPrecision(javaType: String): Int {
        return TYPE_PROPERTY_STRATEGIES
            .first { it.predicate(javaType, "") }
            .defaultPrecision
    }
    
    /**
     * 获取默认小数位数（用于 DECIMAL）
     * @param javaType Java 类型全限定名或简单名
     * @return 默认小数位数，-1 表示不适用
     */
    fun getDefaultScale(javaType: String): Int {
        return TYPE_PROPERTY_STRATEGIES
            .first { it.predicate(javaType, "") }
            .defaultScale
    }
    
    /**
     * 获取类型的完整属性
     * @return TypeProperties 包含长度、精度、小数位数的属性对象
     */
    fun getTypeProperties(javaType: String, fieldName: String = ""): TypeProperties {
        val strategy = TYPE_PROPERTY_STRATEGIES.first { it.predicate(javaType, fieldName) }
        return TypeProperties(
            length = strategy.defaultLength,
            precision = strategy.defaultPrecision,
            scale = strategy.defaultScale
        )
    }
}

/**
 * 类型属性数据类
 * @param length 长度 (-1 表示不适用)
 * @param precision 精度 (-1 表示不适用)
 * @param scale 小数位数 (-1 表示不适用)
 */
data class TypeProperties(
    val length: Int = -1,
    val precision: Int = -1,
    val scale: Int = -1
)

// ============ LsiType 扩展函数 ============

/**
 * 获取类型的默认长度
 */
fun site.addzero.util.lsi.type.LsiType.getDefaultLength(fieldName: String = ""): Int {
    return TypeMapper.getDefaultLength(this.qualifiedName ?: this.name ?: "", fieldName)
}

/**
 * 获取类型的默认精度
 */
fun site.addzero.util.lsi.type.LsiType.getDefaultPrecision(): Int {
    return TypeMapper.getDefaultPrecision(this.qualifiedName ?: this.name ?: "")
}

/**
 * 获取类型的默认小数位数
 */
fun site.addzero.util.lsi.type.LsiType.getDefaultScale(): Int {
    return TypeMapper.getDefaultScale(this.qualifiedName ?: this.name ?: "")
}

/**
 * 获取类型的完整属性
 */
fun site.addzero.util.lsi.type.LsiType.getTypeProperties(fieldName: String = ""): TypeProperties {
    return TypeMapper.getTypeProperties(this.qualifiedName ?: this.name ?: "", fieldName)
}
