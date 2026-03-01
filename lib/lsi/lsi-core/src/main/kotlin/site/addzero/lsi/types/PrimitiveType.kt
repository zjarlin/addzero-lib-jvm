package site.addzero.lsi.types

/**
 * 基本类型的强类型枚举
 *
 * 统一管理Java和Kotlin的基本类型及其默认值
 */

enum class PrimitiveType(
    val kotlinName: String,
    val javaName: String,
    val javaWrapperFqName: String,
    val defaultValue: Any
) {
    INT("Int", "int", "java.lang.Integer", 0),
    LONG("Long", "long", "java.lang.Long", 0L),
    SHORT("Short", "short", "java.lang.Short", 0.toShort()),
    BYTE("Byte", "byte", "java.lang.Byte", 0.toByte()),
    FLOAT("Float", "float", "java.lang.Float", 0.0f),
    DOUBLE("Double", "double", "java.lang.Double", 0.0),
    BOOLEAN("Boolean", "boolean", "java.lang.Boolean", false),
    CHAR("Char", "char", "java.lang.Character", ' ');

    companion object {
        private val byKotlinName = entries.associateBy { it.kotlinName.lowercase() }
        private val byJavaName = entries.associateBy { it.javaName.lowercase() }
        private val byWrapperFqName = entries.associateBy { it.javaWrapperFqName }

        fun findByName(name: String): PrimitiveType? {
            val lower = name.substringAfterLast('.').lowercase()
            return byKotlinName[lower] ?: byJavaName[lower] ?: byWrapperFqName[name]
        }

        fun isPrimitive(name: String): Boolean = findByName(name) != null

        val allKotlinNames: Set<String> get() = entries.map { it.kotlinName }.toSet()
        val allJavaNames: Set<String> get() = entries.map { it.javaName }.toSet()
        val allWrapperFqNames: Set<String> get() = entries.map { it.javaWrapperFqName }.toSet()
    }
}
