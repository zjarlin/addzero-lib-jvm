package site.addzero.lsi.type

import site.addzero.lsi.types.TypeRegistry

fun LsiType.isDateOrTime(): Boolean {
    val typeName = typeNameForTypeRegistry() ?: return false
    return TypeRegistry.isDateTime(typeName)
}

fun LsiType.isDate(): Boolean {
    val typeName = typeNameForTypeRegistry() ?: return false
    return TypeRegistry.isDate(typeName)
}

fun LsiType.isTime(): Boolean {
    val typeName = typeNameForTypeRegistry() ?: return false
    return TypeRegistry.isTime(typeName)
}

private fun LsiType.typeNameForTypeRegistry(): String? {
    val raw = qualifiedName ?: presentableText ?: simpleName ?: return null
    return raw.normalizeTypeName()
}

private fun String.normalizeTypeName(): String {
    var normalized = trim()
    normalized = normalized.substringBefore('<')
    normalized = normalized.removeSuffix("?").removeSuffix("!").trim()
    while (normalized.endsWith("[]")) {
        normalized = normalized.removeSuffix("[]")
    }
    return normalized
}
