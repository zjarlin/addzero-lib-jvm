package site.addzero.ioc.strategy


class RegularFunctionStrategy : CodeGenerationStrategy {
    override fun generateCollectionCode(functions: List<BeanInfo>) = """
    val collectRegular = listOf(
        ${functions.joinToString(",\n        ") { "{ ${it.name}() }" }}
    )
    """.trimIndent()

    override fun generateExecuteMethod() = """
    fun iocRegularStart() {
        collectRegular.forEach { it() }
    }
    """.trimIndent()
}

class ClassInstanceStrategy : CodeGenerationStrategy {
    override fun generateCollectionCode(functions: List<BeanInfo>) = """
    val collectClassInstance = listOf(
        ${functions.joinToString(",\n        ") { "{ ${it.name}() }" }}
    )
    """.trimIndent()

    override fun generateExecuteMethod() = """
    fun iocClassInstanceStart() {
        collectClassInstance.forEach { it() }
    }
    """.trimIndent()
}

class ObjectInstanceStrategy : CodeGenerationStrategy {
    override fun generateCollectionCode(functions: List<BeanInfo>) = """
    val collectObjectInstance = listOf(
        ${functions.joinToString(",\n        ") { "{ ${it.name} }" }}
    )
    """.trimIndent()

    override fun generateExecuteMethod() = """
    fun iocObjectInstanceStart() {
        collectObjectInstance.forEach { it() }
    }
    """.trimIndent()
}

class SuspendFunctionStrategy : CodeGenerationStrategy {
    override fun generateCollectionCode(functions: List<BeanInfo>) = """
    val collectSuspend = listOf(
        ${functions.joinToString(",\n        ") { "suspend { ${it.name}() }" }}
    )
    """.trimIndent()

    override fun generateExecuteMethod() = """
    suspend fun iocSuspendStart() {
        collectSuspend.forEach { it() }
    }
    """.trimIndent()
}

class ComposableFunctionStrategy : CodeGenerationStrategy {
    override fun generateCollectionCode(functions: List<BeanInfo>) = """
    val collectComposable = listOf(
        ${functions.joinToString(",\n        ") { "@androidx.compose.runtime.Composable { ${it.name}() }" }}
    )
    """.trimIndent()

    override fun generateExecuteMethod() = """
    @androidx.compose.runtime.Composable
    fun IocComposeableStart() {
        collectComposable.forEach { it() }
    }
    """.trimIndent()
}
