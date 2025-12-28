package site.addzero.ioc.container

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import site.addzero.ioc.strategy.ClassInstanceStrategy
import site.addzero.ioc.strategy.ComposableFunctionStrategy
import site.addzero.ioc.strategy.InitType
import site.addzero.ioc.strategy.ObjectInstanceStrategy
import site.addzero.ioc.strategy.RegularFunctionStrategy
import site.addzero.ioc.strategy.SuspendFunctionStrategy

class ContainerGenerator(private val codeGenerator: CodeGenerator) {
    private val strategies = mapOf(
        "regular" to RegularFunctionStrategy(),
        "classInstance" to ClassInstanceStrategy(),
        "objectInstance" to ObjectInstanceStrategy(),
        "suspend" to SuspendFunctionStrategy(),
        "composable" to ComposableFunctionStrategy()
    )

    fun generate(functions: List<Pair<String, InitType>>) {
        if (functions.isEmpty()) return

        val regularFunctions = functions.filter { (_, type) ->
            type == InitType.TOP_LEVEL_FUNCTION || type == InitType.COMPANION_OBJECT
        }
        val classInstances = functions.filter { (_, type) -> type == InitType.CLASS_INSTANCE }
        val objectInstances = functions.filter { (_, type) -> type == InitType.OBJECT_INSTANCE }

        val imports = functions.mapNotNull { (name, type) ->
            when (type) {
                InitType.CLASS_INSTANCE, InitType.OBJECT_INSTANCE, InitType.COMPANION_OBJECT -> name
                else -> null
            }
        }.toSet()

        val code = buildString {
            appendLine("package site.addzero.ioc.generated")
            appendLine()
            imports.forEach { appendLine("import $it") }
            appendLine()
            appendLine("public object IocContainer {")

            if (regularFunctions.isNotEmpty()) {
                appendLine(strategies["regular"]!!.generateCollectionCode(regularFunctions).prependIndent("    "))
                appendLine()
                appendLine(strategies["regular"]!!.generateExecuteMethod().prependIndent("    "))
                appendLine()
            }

            if (classInstances.isNotEmpty()) {
                appendLine(strategies["classInstance"]!!.generateCollectionCode(classInstances).prependIndent("    "))
                appendLine()
                appendLine(strategies["classInstance"]!!.generateExecuteMethod().prependIndent("    "))
                appendLine()
            }

            if (objectInstances.isNotEmpty()) {
                appendLine(strategies["objectInstance"]!!.generateCollectionCode(objectInstances).prependIndent("    "))
                appendLine()
                appendLine(strategies["objectInstance"]!!.generateExecuteMethod().prependIndent("    "))
                appendLine()
            }

            val methodNames = mutableListOf<String>()
            if (regularFunctions.isNotEmpty()) methodNames.add("iocRegularStart")
            if (classInstances.isNotEmpty()) methodNames.add("iocClassInstanceStart")
            if (objectInstances.isNotEmpty()) methodNames.add("iocObjectInstanceStart")

            appendLine("    fun iocAllStart() {")
            methodNames.forEach { appendLine("        $it()") }
            appendLine("    }")
            appendLine("}")
        }

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            "site.addzero.ioc.generated",
            "IocContainer",
            "kt"
        ).use { it.write(code.toByteArray()) }
    }
}
