package site.addzero.util.lsi.types

/**
 * 数值类型的强类型枚举
 *
 * 包括整型、浮点型、大数等数值类型
 */
enum class NumericType(
    val kotlinName: String,
    val javaFqName: String,
    val category: NumericCategory,
    val defaultValue: Any
) {
    INT("Int", "java.lang.Integer", NumericCategory.INTEGER, 0),
    LONG("Long", "java.lang.Long", NumericCategory.INTEGER, 0L),
    SHORT("Short", "java.lang.Short", NumericCategory.INTEGER, 0.toShort()),
    BYTE("Byte", "java.lang.Byte", NumericCategory.INTEGER, 0.toByte()),
    FLOAT("Float", "java.lang.Float", NumericCategory.FLOATING_POINT, 0.0f),
    DOUBLE("Double", "java.lang.Double", NumericCategory.FLOATING_POINT, 0.0),
    BIG_DECIMAL("BigDecimal", "java.math.BigDecimal", NumericCategory.BIG_NUMBER, "0.00"),
    BIG_INTEGER("BigInteger", "java.math.BigInteger", NumericCategory.BIG_NUMBER, "0");

    val isInteger: Boolean get() = category == NumericCategory.INTEGER
    val isFloatingPoint: Boolean get() = category == NumericCategory.FLOATING_POINT
    val isBigNumber: Boolean get() = category == NumericCategory.BIG_NUMBER

    companion object {
        private val byKotlinName = entries.associateBy { it.kotlinName.lowercase() }
        private val byFqName = entries.associateBy { it.javaFqName }
        private val bySimpleName = entries.associateBy { it.javaFqName.substringAfterLast('.').lowercase() }
        private val byCategory = entries.groupBy { it.category }

        fun findByName(name: String): NumericType? {
            val simpleName = name.substringAfterLast('.').lowercase()
            return byKotlinName[simpleName] ?: bySimpleName[simpleName] ?: byFqName[name]
        }

        fun isNumeric(name: String): Boolean = findByName(name) != null
        
        fun allInCategory(category: NumericCategory): List<NumericType> = byCategory[category].orEmpty()
        
        val allIntegerTypes: List<NumericType> get() = allInCategory(NumericCategory.INTEGER)
        val allFloatingPointTypes: List<NumericType> get() = allInCategory(NumericCategory.FLOATING_POINT)
        val allBigNumberTypes: List<NumericType> get() = allInCategory(NumericCategory.BIG_NUMBER)
    }
}

/**
 * 数值类型的分类
 */
enum class NumericCategory {
    /** 整型 (Int, Long, Short, Byte) */
    INTEGER,
    /** 浮点型 (Float, Double) */
    FLOATING_POINT,
    /** 大数 (BigDecimal, BigInteger) */
    BIG_NUMBER
}
