package site.addzero.json2kotlin.model

sealed class JsonNode {
    abstract val kotlinType: String
    abstract fun toKotlinValue(): String
}

data class JsonNull(val originalKey: String? = null) : JsonNode() {
    override val kotlinType: String = "Any?"
    override fun toKotlinValue(): String = "null"
}

data class JsonString(val value: String) : JsonNode() {
    override val kotlinType: String = "String"
    override fun toKotlinValue(): String = "\"${value.escape()}\""
    
    private fun String.escape(): String = this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

data class JsonInt(val value: Long) : JsonNode() {
    override val kotlinType: String = if (value in Int.MIN_VALUE..Int.MAX_VALUE) "Int" else "Long"
    override fun toKotlinValue(): String = if (kotlinType == "Long") "${value}L" else value.toString()
}

data class JsonDouble(val value: Double) : JsonNode() {
    override val kotlinType: String = "Double"
    override fun toKotlinValue(): String = value.toString()
}

data class JsonBoolean(val value: Boolean) : JsonNode() {
    override val kotlinType: String = "Boolean"
    override fun toKotlinValue(): String = value.toString()
}

data class JsonArray(val elements: List<JsonNode>, val inferredElementType: String) : JsonNode() {
    override val kotlinType: String = "List<$inferredElementType>"
    override fun toKotlinValue(): String = elements.joinToString(
        prefix = "listOf(",
        postfix = ")",
        separator = ", "
    ) { it.toKotlinValue() }
}

data class JsonObject(
    val className: String,
    val properties: Map<String, JsonNode>
) : JsonNode() {
    override val kotlinType: String = className
    override fun toKotlinValue(): String {
        if (properties.isEmpty()) return "$className()"
        return properties.entries.joinToString(
            prefix = "$className(\n    ",
            postfix = "\n)",
            separator = ",\n    "
        ) { (key, value) -> "${key.toPropertyName()} = ${value.toKotlinValue()}" }
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
    
    companion object {
        private val KOTLIN_KEYWORDS = setOf(
            "as", "break", "class", "continue", "do", "else", "false", "for", "fun",
            "if", "in", "interface", "is", "null", "object", "package", "return",
            "super", "this", "throw", "true", "try", "typealias", "typeof", "val",
            "var", "when", "while"
        )
    }
}
