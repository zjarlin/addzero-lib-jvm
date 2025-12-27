package com.example.autoinit.ksp.strategy

class RegularFunctionStrategy : CodeGenerationStrategy {
    override fun generateCollectionCode(functions: List<Pair<String, InitType>>) = """
    val collectRegular = listOf(
        ${functions.joinToString(",\n        ") { (name, _) -> "{ $name() }" }}
    )
    """.trimIndent()

    override fun generateExecuteMethod() = """
    fun iocRegularStart() {
        collectRegular.forEach { it() }
    }
    """.trimIndent()
}

class ClassInstanceStrategy : CodeGenerationStrategy {
    override fun generateCollectionCode(functions: List<Pair<String, InitType>>) = """
    val collectClassInstance = listOf(
        ${functions.joinToString(",\n        ") { (name, _) -> "{ $name() }" }}
    )
    """.trimIndent()

    override fun generateExecuteMethod() = """
    fun iocClassInstanceStart() {
        collectClassInstance.forEach { it() }
    }
    """.trimIndent()
}

class ObjectInstanceStrategy : CodeGenerationStrategy {
    override fun generateCollectionCode(functions: List<Pair<String, InitType>>) = """
    val collectObjectInstance = listOf(
        ${functions.joinToString(",\n        ") { (name, _) -> "{ $name }" }}
    )
    """.trimIndent()

    override fun generateExecuteMethod() = """
    fun iocObjectInstanceStart() {
        collectObjectInstance.forEach { it() }
    }
    """.trimIndent()
}

class SuspendFunctionStrategy : CodeGenerationStrategy {
    override fun generateCollectionCode(functions: List<Pair<String, InitType>>) = """
    val collectSuspend = listOf(
        ${functions.joinToString(",\n        ") { (name, _) -> "suspend { $name() }" }}
    )
    """.trimIndent()

    override fun generateExecuteMethod() = """
    suspend fun iocSuspendStart() {
        collectSuspend.forEach { it() }
    }
    """.trimIndent()
}

class ComposableFunctionStrategy : CodeGenerationStrategy {
    override fun generateCollectionCode(functions: List<Pair<String, InitType>>) = """
    val collectComposable = listOf(
        ${functions.joinToString(",\n        ") { (name, _) -> "@androidx.compose.runtime.Composable { $name() }" }}
    )
    """.trimIndent()

    override fun generateExecuteMethod() = """
    @androidx.compose.runtime.Composable
    fun IocComposeableStart() {
        collectComposable.forEach { it() }
    }
    """.trimIndent()
}
