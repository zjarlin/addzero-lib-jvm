package site.addzero.gradle.plugin

import org.gradle.util.internal.TextUtil.toLowerCamelCase

object CodeGenHelper {

    fun inferType(value: String): String {
        // 支持三引号包裹的 Kotlin 表达式
        if (value.trimStart().startsWith("\"\"\"")) {
            val expr = value.trim().removeSurrounding("\"\"\"")
            return inferTypeFromKotlinExpr(expr)
        }

        // 支持转义形式的 Kotlin 表达式: listOf(\"a\")
        if (value.contains("\\\"")) {
            val expr = value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
            return inferTypeFromKotlinExpr(expr.trim())
        }

        // 支持直接传入 Kotlin 表达式字符串: listOf("a"), setOf("b")
        val trimmed = value.trim()
        if (trimmed.startsWith("listOf(") || trimmed.startsWith("setOf(") ||
            trimmed.startsWith("emptyList()") || trimmed.startsWith("emptySet()")
        ) {
            return inferTypeFromKotlinExpr(trimmed)
        }

        // 优先检测列表类型
        val listType = detectListType(value)
        if (listType != null) {
            return listType.second
        }
        return when {
            value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true) -> "Boolean"
            value.toIntOrNull() != null && !value.startsWith("0") && !value.contains(".") -> "Int"
            value.toLongOrNull() != null && !value.contains(".") -> "Long"
            value.toDoubleOrNull() != null && value.contains(".") -> "Double"
            else -> "String"
        }
    }

    fun inferTypeFromKotlinExpr(expr: String): String {
        val trimmed = expr.trim()
        return when {
            trimmed.startsWith("listOf(") -> {
                val inner = trimmed.removePrefix("listOf(").removeSuffix(")")
                val elementType = inferElementTypeFromListInner(inner)
                "List<$elementType>"
            }
            trimmed.startsWith("setOf(") -> {
                val inner = trimmed.removePrefix("setOf(").removeSuffix(")")
                val elementType = inferElementTypeFromListInner(inner)
                "Set<$elementType>"
            }
            trimmed.startsWith("emptyList()") -> "List<Any>"
            trimmed.startsWith("emptySet()") -> "Set<Any>"
            trimmed.equals("true", ignoreCase = true) || trimmed.equals("false", ignoreCase = true) -> "Boolean"
            trimmed.toIntOrNull() != null && !trimmed.startsWith("0") && !trimmed.contains(".") -> "Int"
            trimmed.toLongOrNull() != null && !trimmed.contains(".") -> "Long"
            trimmed.toDoubleOrNull() != null && trimmed.contains(".") -> "Double"
            else -> "String"
        }
    }

    fun inferElementTypeFromListInner(inner: String): String {
        val parts = inner.split(",").map { it.trim() }
        if (parts.isEmpty()) return "Any"

        return when {
            parts.all { it.equals("true", ignoreCase = true) || it.equals("false", ignoreCase = true) } -> "Boolean"
            parts.all { it.toIntOrNull() != null && !it.startsWith("0") && !it.contains(".") } -> "Int"
            parts.all { it.toLongOrNull() != null && !it.contains(".") } -> "Long"
            parts.all { it.toDoubleOrNull() != null } -> "Double"
            parts.all { it.startsWith("\"") && it.endsWith("\"") } -> "String"
            else -> "String"
        }
    }

    fun detectListType(value: String): Pair<Boolean, String>? {
        if (!value.contains(",") || value.startsWith("\"")) {
            return null
        }
        val parts = value.split(",").map { it.trim() }
        if (parts.size < 2) return null
        val nonEmptyParts = parts.filter { it.isNotEmpty() }

        if (nonEmptyParts.isEmpty()) {
            return true to "List<String>"
        }

        val elementType = when {
            nonEmptyParts.all { it.equals("true", ignoreCase = true) || it.equals("false", ignoreCase = true) } -> "Boolean"
            nonEmptyParts.all { it.toIntOrNull() != null && !it.startsWith("0") && !it.contains(".") } -> "Int"
            nonEmptyParts.all { it.toLongOrNull() != null && !it.contains(".") } -> "Long"
            nonEmptyParts.all { it.toDoubleOrNull() != null } -> "Double"
            else -> "String"
        }
        return true to "List<$elementType>"
    }

    fun toDefaultValueExpression(value: String, type: String): String {
        // 支持三引号包裹的 Kotlin 表达式
        if (value.trimStart().startsWith("\"\"\"")) {
            return value.trim().removeSurrounding("\"\"\"")
        }

        // 支持转义形式的 Kotlin 表达式: listOf(\"a\")
        if (value.contains("\\\"")) {
            return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
        }

        // 支持直接传入 Kotlin 表达式字符串: listOf("a"), setOf("b")
        val trimmedVal = value.trim()
        if (trimmedVal.startsWith("listOf(") || trimmedVal.startsWith("setOf(") ||
            trimmedVal.startsWith("emptyList()") || trimmedVal.startsWith("emptySet()")
        ) {
            return trimmedVal
        }

        val listType = detectListType(value)
        return if (listType != null && type.startsWith("List<")) {
            val listTypeStr = listType.second
            val elementType = listTypeStr.removePrefix("List<").removeSuffix(">")
            val parts = value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.isEmpty()) {
                return "emptyList()"
            }
            val elementValues = when (elementType) {
                "String" -> parts.joinToString(", ") { "\"$it\"" }
                "Int" -> parts.joinToString(", ") { it }
                "Long" -> parts.joinToString(", ") { "${it}L" }
                "Double" -> parts.joinToString(", ") { it }
                "Boolean" -> parts.joinToString(", ") { it.lowercase() }
                else -> parts.joinToString(", ")
            }
            "listOf($elementValues)"
        } else {
            when (type) {
                "Boolean" -> value.toBoolean().toString()
                "Int" -> (value.toIntOrNull() ?: 0).toString()
                "Long" -> "${value.toLongOrNull() ?: 0L}L"
                "Double" -> "${value.toDoubleOrNull() ?: 0.0}"
                else -> "\"$value\""
            }
        }
    }

    fun toSerializationExpression(optionKey: String, propertyName: String, type: String): String {
        return if (type.startsWith("List<") || type.startsWith("Set<")) {
            """        "$optionKey" to ${propertyName}.joinToString(",")"""
        } else {
            """        "$optionKey" to ${propertyName}.toString()"""
        }
    }

    fun toFromOptionsExpression(optionKey: String, propertyName: String, type: String, defaultValue: String): String {
        val defaultExpr = toDefaultValueExpression(defaultValue, type)
        return if (type.startsWith("List<")) {
            val elementType = type.removePrefix("List<").removeSuffix(">")
            val parseExpr = when (elementType) {
                "String" -> "it"
                "Int" -> "it.toIntOrNull()"
                "Long" -> "it.toLongOrNull()"
                "Double" -> "it.toDoubleOrNull()"
                "Boolean" -> "it.toBoolean()"
                else -> "it"
            }
            val filterExpr = if (elementType != "String") "?.filterNotNull()" else ""
            """        this.$propertyName = options["$optionKey"]?.split(",")?.filter { it.isNotEmpty() }?.map { $parseExpr }$filterExpr ?: $defaultExpr"""
        } else {
            when (type) {
                "Boolean" -> """        this.$propertyName = options["$optionKey"]?.toBoolean() ?: $defaultExpr"""
                "Int" -> """        this.$propertyName = options["$optionKey"]?.toIntOrNull() ?: $defaultExpr"""
                "Long" -> """        this.$propertyName = options["$optionKey"]?.toLongOrNull() ?: $defaultExpr"""
                "Double" -> """        this.$propertyName = options["$optionKey"]?.toDoubleOrNull() ?: $defaultExpr"""
                else -> """        this.$propertyName = options["$optionKey"] ?: $defaultExpr"""
            }
        }
    }

    fun toLowerCamelCaseKey(key: String): String = toLowerCamelCase(key)
}
