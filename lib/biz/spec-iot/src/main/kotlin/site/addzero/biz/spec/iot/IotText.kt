package site.addzero.biz.spec.iot

internal fun requireText(value: String?, field: String): String {
    val trimmed = value?.trim()
    require(!trimmed.isNullOrEmpty()) { "$field must not be blank" }
    return trimmed
}

internal fun trimToNull(value: String?): String? {
    return value?.trim()?.takeIf { it.isNotEmpty() }
}
