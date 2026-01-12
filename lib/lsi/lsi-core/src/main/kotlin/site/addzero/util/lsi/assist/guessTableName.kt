package site.addzero.util.lsi.assist

import site.addzero.util.lsi.types.DocumentationAnnotationType
import site.addzero.util.str.removeAnyQuote
import site.addzero.util.str.toNotEmptyStr
import site.addzero.util.str.toUnderLineCase

fun guessTableName(fqName: String, getAttr: (String) -> Any?): Any? {
    val mapOf = mapOf(
        "com.baomidou.mybatisplus.annotation.TableName" to "value",
        "org.babyfish.jimmer.sql.Table" to "name",
        "javax.persistence.Table" to "value",
        "jakarta.persistence.Table" to "value"
    )
    val argName = mapOf[fqName] ?: return null
    val attr = getAttr(argName)
    return attr

}

fun <T> Iterator<T>.guessTableNameOrNull(getQualifiedName: (T) -> String, getArg: (T, String) -> Any?): String? {
    val tableNameFromAnno = this.asSequence().map { t ->
        val guessTableName = guessTableName(getQualifiedName(t)) {
            getArg(t, it)
        }
        guessTableName.toNotEmptyStr().toUnderLineCase()
    }.firstOrNull()
    return tableNameFromAnno?.removeAnyQuote()
}

/**
 * 从注解中猜测字段注释
 *
 * 支持的注解及其属性映射：
 * - ApiModelProperty, ExcelProperty (Alibaba/IDev): value 属性
 * - Schema (OpenAPI): description 属性
 * - Excel (Easy-POI): name 属性
 *
 * @param getQualifiedName 获取注解的全限定名
 * @param getAttributeValue 获取注解的指定属性值
 * @return 推断的字段注释，如果没有找到则返回 null
 */
fun <T> Iterator<T>.guessFieldCommentOrNull(
    getQualifiedName: (T) -> String, getAttributeValue: (T, String) -> Any?
): String? {
    return this.asSequence().mapNotNull { annotation ->
        val fqName = getQualifiedName(annotation)
        val comment = guessFieldComment(fqName) { attrName ->
            getAttributeValue(annotation, attrName)
        }
        comment?.toString()?.removeAnyQuote()
    }.firstOrNull()
}

/**
 * 根据注解全限定名猜测字段注释
 *
 * @param fqName 注解的全限定名
 * @param getAttr 获取注解属性值的函数
 * @return 推断的字段注释，如果没有找到则返回 null
 */
fun guessFieldComment(fqName: String, getAttr: (String) -> Any?): Any? {
    val attrName = FIELD_COMMENT_ANNOTATION_MAP[fqName] ?: return null
    return getAttr(attrName)
}

/**
 * 根据注解全限定名猜测方法/接口注释
 */
fun guessMethodComment(fqName: String, getAttr: (String) -> Any?): Any? {
    val attrName = METHOD_COMMENT_ANNOTATION_MAP[fqName] ?: return null
    return getAttr(attrName)
}

/**
 * 根据注解全限定名猜测类注释
 */
fun guessClassComment(fqName: String, getAttr: (String) -> Any?): Any? {
    val attrName = CLASS_COMMENT_ANNOTATION_MAP[fqName] ?: return null
    return getAttr(attrName)
}

/**
 * 从注解中猜测方法注释
 */
fun <T> Iterator<T>.guessMethodCommentOrNull(
    getQualifiedName: (T) -> String, getAttributeValue: (T, String) -> Any?
): String? {
    return this.asSequence().mapNotNull { annotation ->
        val fqName = getQualifiedName(annotation)
        val comment = guessMethodComment(fqName) { attrName ->
            getAttributeValue(annotation, attrName)
        }
        comment?.toString()?.removeAnyQuote()
    }.firstOrNull { it.isNotBlank() }
}

/**
 * 从注解中猜测类注释
 */
fun <T> Iterator<T>.guessClassCommentOrNull(
    getQualifiedName: (T) -> String, getAttributeValue: (T, String) -> Any?
): String? {
    return this.asSequence().mapNotNull { annotation ->
        val fqName = getQualifiedName(annotation)
        val comment = guessClassComment(fqName) { attrName ->
            getAttributeValue(annotation, attrName)
        }
        comment?.toString()?.removeAnyQuote()
    }.firstOrNull { it.isNotBlank() }
}


// 字段注释注解映射 - 使用枚举生成
private val FIELD_COMMENT_ANNOTATION_MAP = DocumentationAnnotationType.fqNameToAttributeMap

// 方法注释注解映射
private val METHOD_COMMENT_ANNOTATION_MAP = mapOf(
    // Swagger2
    "io.swagger.annotations.ApiOperation" to "value",
    // Swagger3 / OpenAPI
    "io.swagger.v3.oas.annotations.Operation" to "summary",
    // JAX-RS
    "javax.ws.rs.Path" to "value"
)

// 类注释注解映射
private val CLASS_COMMENT_ANNOTATION_MAP = mapOf(
    // Swagger2
    "io.swagger.annotations.Api" to "tags",
    // Swagger3 / OpenAPI
    "io.swagger.v3.oas.annotations.tags.Tag" to "name"
)




