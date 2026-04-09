@file:JvmName("JvmGeneratedIdentifierUtil")

package site.addzero.util.jvmstr

/**
 * 兼容旧包名，实际实现已迁到 tool-str。
 */
@Deprecated(
    message = "请改用 site.addzero.util.str.toGeneratedMethodName",
    replaceWith = ReplaceWith("this.toGeneratedMethodName(defaultName)", "site.addzero.util.str.toGeneratedMethodName"),
)
fun String.toGeneratedMethodName(
    defaultName: String = "generatedMethod",
): String {
    return site.addzero.util.str.toGeneratedMethodName(this, defaultName)
}

/**
 * 兼容旧包名，实际实现已迁到 tool-str。
 */
@Deprecated(
    message = "请改用 site.addzero.util.str.toGeneratedPropertyName",
    replaceWith = ReplaceWith("this.toGeneratedPropertyName(defaultName)", "site.addzero.util.str.toGeneratedPropertyName"),
)
fun String.toGeneratedPropertyName(
    defaultName: String = "fieldValue",
): String {
    return site.addzero.util.str.toGeneratedPropertyName(this, defaultName)
}

/**
 * 兼容旧包名，实际实现已迁到 tool-str。
 */
@Deprecated(
    message = "请改用 site.addzero.util.str.toGeneratedTypeName",
    replaceWith = ReplaceWith("this.toGeneratedTypeName(defaultName)", "site.addzero.util.str.toGeneratedTypeName"),
)
fun String.toGeneratedTypeName(
    defaultName: String = "GeneratedModel",
): String {
    return site.addzero.util.str.toGeneratedTypeName(this, defaultName)
}
