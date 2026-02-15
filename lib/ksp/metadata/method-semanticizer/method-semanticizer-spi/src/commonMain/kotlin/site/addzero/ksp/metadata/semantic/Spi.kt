package site.addzero.ksp.metadata.semantic

/**
 * 语义化方法定义模型
 */
data class SemanticMethodDefinition(
    val newMethodName: String,
    val fixedParameters: Map<String, Any?>,
    val doc: String? = null
)

/**
 * 方法语义化映射提供者 SPI
 */
interface SemanticMappingProvider {
    /**
     * 该 Provider 主动声明要处理的类的全限定名列表
     */
    fun getSupportedClassNames(): List<String> = emptyList()

    /**
     * 获取指定类的语义化映射表
     */
    fun getMappings(qualifiedName: String): Map<String, List<SemanticMethodDefinition>>?
}

/**
 * 语义化映射表 DSL 构建器 (保留在 SPI 层，因为它不含复杂逻辑，仅为方便构建模型)
 */
class SemanticTable {
    private val mappings = mutableMapOf<String, MutableList<SemanticMethodDefinition>>()

    fun method(originMethod: String, block: MethodContext.() -> Unit): SemanticTable {
        val context = MethodContext(originMethod)
        context.block()
        mappings.getOrPut(originMethod) { mutableListOf() }.addAll(context.definitions)
        return this
    }

    fun build(): Map<String, List<SemanticMethodDefinition>> = mappings

    class MethodContext(val originMethod: String) {
        val definitions = mutableListOf<SemanticMethodDefinition>()

        fun variation(name: String, vararg args: Pair<String, Any?>, doc: String? = null) {
            definitions.add(SemanticMethodDefinition(name, args.toMap(), doc))
        }

        fun addAll(list: List<SemanticMethodDefinition>) {
            definitions.addAll(list)
        }
    }
}