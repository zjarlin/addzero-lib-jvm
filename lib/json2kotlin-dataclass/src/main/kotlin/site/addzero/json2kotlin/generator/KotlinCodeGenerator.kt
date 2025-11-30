package site.addzero.json2kotlin.generator

import site.addzero.json2kotlin.model.*
import site.addzero.json2kotlin.parser.ParseResult

class KotlinCodeGenerator(private val config: GeneratorConfig = GeneratorConfig()) {

    fun generate(parseResult: ParseResult, variableName: String = "data"): GeneratedCode {
        val classDefinitions = generateClassDefinitions(parseResult.classDefinitions)
        val instanceAssignment = generateInstanceAssignment(parseResult.rootNode, variableName)

        return GeneratedCode(
            packageDeclaration = config.packageName?.let { "package $it" } ?: "",

            imports = generateImports(),
            classDefinitions = classDefinitions,
            instanceAssignment = instanceAssignment,
            fullCode = buildFullCode(classDefinitions, instanceAssignment)
        )
    }

    private fun generateClassDefinitions(definitions: Map<String, JsonObject>): String {
        return definitions.values
            .distinctBy { it.className }
            .joinToString("\n\n") { generateDataClass(it) }
    }

    private fun generateDataClass(jsonObject: JsonObject): String {
        val className = jsonObject.className
        val properties = jsonObject.properties

        if (properties.isEmpty()) {
            return "data class $className(val placeholder: Unit = Unit)"
        }

        val propsCode = properties.entries.joinToString(",\n    ") { (name, node) ->
            val type = resolveType(node)
            "val $name: $type"
        }

        return """data class $className(
    $propsCode
)"""
    }

    private fun resolveType(node: JsonNode): String = when (node) {
        is JsonNull -> "Any?"
        is JsonString -> "String"
        is JsonInt -> node.kotlinType
        is JsonDouble -> "Double"
        is JsonBoolean -> "Boolean"
        is JsonArray -> "List<${node.inferredElementType}>"
        is JsonObject -> node.className
    }

    private fun generateInstanceAssignment(rootNode: JsonNode, variableName: String): String {
        val valueCode = rootNode.toKotlinValue()
        val type = rootNode.kotlinType
        return "val $variableName: $type = $valueCode"
    }

    private fun generateImports(): String {
        return "" // 基本类型不需要额外导入
    }

    private fun buildFullCode(classDefinitions: String, instanceAssignment: String): String {
        val parts = mutableListOf<String>()

        config.packageName?.let { parts.add("package $it") }

        if (classDefinitions.isNotBlank()) {
            parts.add(classDefinitions)
        }

        parts.add(instanceAssignment)

        return parts.joinToString("\n\n")
    }
}

data class GeneratorConfig(
    val packageName: String? = null,
    val indent: String = "    ",
    val nullableByDefault: Boolean = false
)

data class GeneratedCode(
    val packageDeclaration: String,
    val imports: String,
    val classDefinitions: String,
    val instanceAssignment: String,
    val fullCode: String
)
