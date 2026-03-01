package site.addzero.lsi.assist

import site.addzero.lsi.types.PojoAnnotationCategory
import site.addzero.lsi.types.PojoAnnotationType

/**
 * 判断注解全限定名是否为POJO相关注解
 */
fun String.isPojoAnnotation(): Boolean = PojoAnnotationType.isPojoAnnotation(this)

/**
 * 判断注解简单名是否为POJO相关注解
 */
fun String.isPojoAnnotationShort(): Boolean = PojoAnnotationType.findByShortName(this).isNotEmpty()

/**
 * 从注解名称列表判断是否为POJO
 * @param isShortName 是否为简单名称
 */
fun Iterable<String>.hasPojoAnnotation(isShortName: Boolean = false): Boolean {
    return if (isShortName) {
        any { it.isPojoAnnotationShort() }
    } else {
        any { it.isPojoAnnotation() }
    }
}

/**
 * 从注解名称列表判断是否有实体注解
 */
fun Iterable<String>.hasEntityAnnotation(isShortName: Boolean = false): Boolean {
    return if (isShortName) {
        any { PojoAnnotationType.findByShortName(it).any { anno -> anno.category == PojoAnnotationCategory.ENTITY } }
    } else {
        any { PojoAnnotationType.findByFqName(it)?.category == PojoAnnotationCategory.ENTITY }
    }
}

/**
 * 从注解名称列表判断是否有表名注解
 */
fun Iterable<String>.hasTableAnnotation(isShortName: Boolean = false): Boolean {
    return if (isShortName) {
        any { PojoAnnotationType.findByShortName(it).any { anno -> anno.category == PojoAnnotationCategory.TABLE } }
    } else {
        any { PojoAnnotationType.findByFqName(it)?.category == PojoAnnotationCategory.TABLE }
    }
}

/**
 * 从注解名称列表判断是否有Lombok注解
 */
fun Iterable<String>.hasLombokAnnotation(isShortName: Boolean = false): Boolean {
    return if (isShortName) {
        any { PojoAnnotationType.findByShortName(it).any { anno -> anno.category == PojoAnnotationCategory.LOMBOK } }
    } else {
        any { PojoAnnotationType.findByFqName(it)?.category == PojoAnnotationCategory.LOMBOK }
    }
}

/**
 * 通用的isPojo判断逻辑
 *
 * @param isInterface 是否为接口
 * @param isEnum 是否为枚举
 * @param isAbstract 是否为抽象类
 * @param isDataClass 是否为Kotlin data class
 * @param annotationNames 注解名称列表
 * @param isShortName 注解名称是否为简单名
 */
fun checkIsPojo(
    isInterface: Boolean,
    isEnum: Boolean,
    isAbstract: Boolean,
    isDataClass: Boolean = false,
    annotationNames: Iterable<String>,
    isShortName: Boolean = false
): Boolean {
    // 排除枚举
    if (isEnum) return false

    val hasEntityAnnotation = annotationNames.hasEntityAnnotation(isShortName)
    val hasTableAnnotation = annotationNames.hasTableAnnotation(isShortName)
    val hasLombokAnnotation = annotationNames.hasLombokAnnotation(isShortName)

    // Jimmer实体是interface但有@Entity注解，也识别为POJO
    if (isInterface) {
        return hasEntityAnnotation || hasTableAnnotation
    }

    // 抽象类只有带实体注解才认为是POJO
    if (isAbstract) {
        return hasEntityAnnotation || hasTableAnnotation
    }

    // 非抽象类：有任何相关注解或是data class即可
    return hasEntityAnnotation || hasTableAnnotation || hasLombokAnnotation || isDataClass
}
