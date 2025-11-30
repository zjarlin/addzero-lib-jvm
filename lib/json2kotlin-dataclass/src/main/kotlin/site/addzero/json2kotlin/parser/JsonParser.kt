package site.addzero.json2kotlin.parser

import com.google.gson.JsonElement
import com.google.gson.JsonParser as GsonParser
import site.addzero.json2kotlin.model.*

class JsonParser(private val config: ParserConfig = ParserConfig()) {
    
    private val classDefinitions = mutableMapOf<String, JsonObject>()
    private var classCounter = 0
    
    fun parse(json: String, rootClassName: String = "Root"): ParseResult {
        classDefinitions.clear()
        classCounter = 0
        
        val element = GsonParser.parseString(json)
        val rootNode = parseElement(element, rootClassName)
        
        return ParseResult(
            rootNode = rootNode,
            classDefinitions = classDefinitions.toMap()
        )
    }
    
    private fun parseElement(element: JsonElement, suggestedName: String): JsonNode = when {
        element.isJsonNull -> JsonNull(suggestedName)
        element.isJsonPrimitive -> parsePrimitive(element.asJsonPrimitive)
        element.isJsonArray -> parseArray(element.asJsonArray, suggestedName)
        element.isJsonObject -> parseObject(element.asJsonObject, suggestedName)
        else -> JsonNull(suggestedName)
    }
    
    private fun parsePrimitive(primitive: com.google.gson.JsonPrimitive): JsonNode = when {
        primitive.isBoolean -> JsonBoolean(primitive.asBoolean)
        primitive.isNumber -> {
            val num = primitive.asNumber
            val doubleVal = num.toDouble()
            if (doubleVal == doubleVal.toLong().toDouble()) {
                JsonInt(num.toLong())
            } else {
                JsonDouble(doubleVal)
            }
        }
        primitive.isString -> JsonString(primitive.asString)
        else -> JsonNull()
    }
    
    private fun parseArray(array: com.google.gson.JsonArray, suggestedName: String): JsonArray {
        if (array.isEmpty) {
            return JsonArray(emptyList(), "Any")
        }
        
        val elements = array.map { parseElement(it, suggestedName.toSingular()) }
        val inferredType = inferArrayElementType(elements)
        
        return JsonArray(elements, inferredType)
    }
    
    private fun parseObject(obj: com.google.gson.JsonObject, suggestedName: String): JsonObject {
        val className = suggestedName.toClassName()
        val properties = obj.entrySet().associate { (key, value) ->
            val propName = key.toPropertyName()
            val childClassName = key.toClassName()
            propName to parseElement(value, childClassName)
        }
        
        val jsonObject = JsonObject(className, properties)
        classDefinitions[className] = jsonObject
        
        return jsonObject
    }
    
    private fun inferArrayElementType(elements: List<JsonNode>): String {
        if (elements.isEmpty()) return "Any"
        
        val types = elements.map { it.kotlinType }.distinct()
        return when {
            types.size == 1 -> types.first()
            types.all { it in setOf("Int", "Long") } -> "Long"
            types.all { it in setOf("Int", "Long", "Double") } -> "Double"
            types.any { it.firstOrNull()?.isUpperCase() == true && it !in setOf("Int", "Long", "Double", "String", "Boolean") } -> {
                types.first { it.firstOrNull()?.isUpperCase() == true && it !in setOf("Int", "Long", "Double", "String", "Boolean") }
            }
            else -> "Any"
        }
    }
    
    private fun String.toClassName(): String {
        if (this.isEmpty()) return "Class${++classCounter}"
        return this.split("_", "-", " ")
            .joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }
    }
    
    private fun String.toPropertyName(): String {
        if (this.isEmpty()) return "_empty"
        val result = StringBuilder()
        var capitalizeNext = false
        for ((index, char) in this.withIndex()) {
            when {
                char == '_' || char == '-' || char == ' ' -> capitalizeNext = true
                index == 0 -> result.append(char.lowercaseChar())
                capitalizeNext -> {
                    result.append(char.uppercaseChar())
                    capitalizeNext = false
                }
                else -> result.append(char)
            }
        }
        val name = result.toString()
        return if (name in KOTLIN_KEYWORDS) "`$name`" else name
    }
    
    private fun String.toSingular(): String = when {
        this.endsWith("ies") -> this.dropLast(3) + "y"
        this.endsWith("es") -> this.dropLast(2)
        this.endsWith("s") && this.length > 1 -> this.dropLast(1)
        else -> this
    }
    
    companion object {
        private val KOTLIN_KEYWORDS = setOf(
            "as", "break", "class", "continue", "do", "else", "false", "for", "fun",
            "if", "in", "interface", "is", "null", "object", "package", "return",
            "super", "this", "throw", "true", "try", "typealias", "typeof", "val",
            "var", "when", "while"
        )
    }
}

data class ParserConfig(
    val nullableByDefault: Boolean = false,
    val useDataClass: Boolean = true,
    val generateComments: Boolean = false
)

data class ParseResult(
    val rootNode: JsonNode,
    val classDefinitions: Map<String, JsonObject>
)
