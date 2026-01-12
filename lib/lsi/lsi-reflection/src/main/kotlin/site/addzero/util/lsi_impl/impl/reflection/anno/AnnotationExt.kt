package site.addzero.util.lsi_impl.impl.reflection.anno

import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.assist.guessFieldCommentOrNull as guessFieldCommentOrNullGeneric

/**
 * Java Annotation 反射扩展函数
 * 
 * 这些扩展提供了 Annotation 到 LsiAnnotation 的转换，以及常用的注解操作
 */

// ==================== LSI 转换 ====================

/**
 * 将 Java Annotation 转换为 LsiAnnotation
 */
fun Annotation.toLsiAnnotation(): LsiAnnotation = ClazzLsiAnnotation(this)

/**
 * 将 Annotation 数组转换为 LsiAnnotation 列表
 */
fun Array<Annotation>.toLsiAnnotations(): List<LsiAnnotation> = map { it.toLsiAnnotation() }

// ==================== 注解属性访问 ====================

/**
 * 提取公共逻辑：通过反射调用注解方法获取字符串值
 */
fun Annotation.getArg(methodName: String): String? {
    return try {
        val method = this.annotationClass.java.getDeclaredMethod(methodName)
        method.invoke(this) as? String
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 获取注解的 value 属性（默认属性）
 */
fun Annotation.getArg(): String? {
    return getArg("value")
}

/**
 * 获取注解的所有属性
 */
fun Annotation.attributes(): Map<String, Any?> {
    return this.annotationClass.java.declaredMethods.associate { method ->
        method.name to try {
            method.invoke(this)
        } catch (e: Exception) {
            null
        }
    }
}

// ==================== 字段注释提取 ====================

/**
 * 从注解数组中提取字段注释
 * 
 * 委托给 lsi-core 的泛型方法 guessFieldCommentOrNull
 * 
 * 遍历所有注解，查找已知的注释注解（如 @Schema, @ApiModelProperty 等），
 * 并返回第一个非空的注释内容
 */
fun Array<Annotation>.fieldComment(): String? {
    return this.iterator().guessFieldCommentOrNullGeneric(
        getQualifiedName = { it.annotationClass.java.name },
        getAttributeValue = { annotation, attrName ->
            annotation.getArg(attrName)
        }
    )
}

/**
 * 从注解数组中猜测字段注释（别名方法，保持向后兼容）
 */
fun Array<Annotation>.guessFieldCommentOrNull(): String? = fieldComment()

// ==================== 注解匹配 ====================

/**
 * 判断注解是否匹配指定的全限定名
 */
fun Annotation.isTargetAnnotation(targetFqName: String): Boolean {
    val fqName = this.annotationClass.java.name
    return fqName == targetFqName
}

/**
 * 判断注解是否匹配指定的简单名称
 */
fun Annotation.isTargetAnnotationBySimpleName(simpleName: String): Boolean {
    return this.annotationClass.java.simpleName == simpleName
}

// ==================== 注解名称访问 ====================

/**
 * 获取注解的全限定名
 */
fun Annotation.qualifiedName(): String {
    return this.annotationClass.java.name
}

/**
 * 获取注解的简单名称
 */
fun Annotation.simpleName(): String {
    return this.annotationClass.java.simpleName
}
