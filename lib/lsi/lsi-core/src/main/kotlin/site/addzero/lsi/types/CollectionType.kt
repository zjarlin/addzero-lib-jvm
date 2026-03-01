package site.addzero.lsi.types

/**
 * 集合类型的强类型枚举
 *
 * 包括Java和Kotlin的各种集合类型
 */
enum class CollectionType(
    val javaFqName: String?, val kotlinFqName: String?, val simpleName: String
) {
    LIST("java.util.List", "kotlin.collections.List", "List"), MUTABLE_LIST(
        null,
        "kotlin.collections.MutableList",
        "MutableList"
    ),
    SET("java.util.Set", "kotlin.collections.Set", "Set"), MUTABLE_SET(
        null,
        "kotlin.collections.MutableSet",
        "MutableSet"
    ),
    MAP("java.util.Map", "kotlin.collections.Map", "Map"), MUTABLE_MAP(
        null,
        "kotlin.collections.MutableMap",
        "MutableMap"
    ),
    COLLECTION("java.util.Collection", "kotlin.collections.Collection", "Collection"), MUTABLE_COLLECTION(
        null,
        "kotlin.collections.MutableCollection",
        "MutableCollection"
    ),
    ARRAY_LIST("java.util.ArrayList", "kotlin.collections.ArrayList", "ArrayList"), LINKED_LIST(
        "java.util.LinkedList",
        "kotlin.collections.LinkedList",
        "LinkedList"
    ),
    HASH_SET("java.util.HashSet", "kotlin.collections.HashSet", "HashSet"), LINKED_HASH_SET(
        "java.util.LinkedHashSet",
        "kotlin.collections.LinkedHashSet",
        "LinkedHashSet"
    ),
    HASH_MAP("java.util.HashMap", "kotlin.collections.HashMap", "HashMap"), LINKED_HASH_MAP(
        "java.util.LinkedHashMap",
        "kotlin.collections.LinkedHashMap",
        "LinkedHashMap"
    );

    companion object {
        private val allFqNames = entries.flatMap {
            listOfNotNull(it.javaFqName, it.kotlinFqName)
        }.toSet()

        private val bySimpleName = entries.groupBy { it.simpleName.lowercase() }

        /**
         * 判断是否为集合类型（包括数组）
         */
        fun isCollection(typeName: String): Boolean {
            return allFqNames.any { typeName.startsWith(it) } || isArray(typeName)
        }

        /**
         * 判断是否为数组类型
         */
        fun isArray(typeName: String): Boolean {
            return typeName.startsWith("Array<") || typeName.endsWith("[]")
        }

        /**
         * 根据简单名称查找集合类型
         */
        fun findBySimpleName(simpleName: String): List<CollectionType> = bySimpleName[simpleName.lowercase()].orEmpty()

        val allJavaFqNames: Set<String> get() = entries.mapNotNull { it.javaFqName }.toSet()
        val allKotlinFqNames: Set<String> get() = entries.mapNotNull { it.kotlinFqName }.toSet()
    }
}
