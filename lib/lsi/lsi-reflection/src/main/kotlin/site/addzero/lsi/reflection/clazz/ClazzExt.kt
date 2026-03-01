package site.addzero.lsi.reflection.clazz

import site.addzero.lsi.assist.guessTableNameOrNull
import site.addzero.lsi.constant.COLLECTION_TYPES
import site.addzero.lsi.reflection.anno.getArg
import site.addzero.lsi.reflection.anno.qualifiedName
import site.addzero.util.str.toUnderLineCase
import java.lang.reflect.Modifier

fun Class<*>.guessTableName(): String {
    // 优先从注解获取表名
    val tableNameFromAnno = this.guessTableNameOrNull()
    return tableNameFromAnno ?: this.simpleName.toUnderLineCase()
}

fun Class<*>.getArrayComponentType(): Class<*>? {
    return if (this.isArray) this.componentType else null
}
fun Class<*>.guessTableNameOrNull(): String? {
    val annotations = this.annotations
    val iterator = annotations.iterator()
    val guessTableNameOrNull = iterator.guessTableNameOrNull({ it.qualifiedName() }, { a, b -> a.getArg(b) })
    return guessTableNameOrNull
}
fun Class<*>.isCollectionType(): Boolean {
    return COLLECTION_TYPES.any { it.isAssignableFrom(this) }
}

/**
 * 判断 Class 类型是否可空
 *
 * 判断逻辑（按优先级）：
 * 1. 如果是基本类型（int, long, boolean 等），返回 false（不可空）
 * 2. 如果类有 @Nullable 注解，返回 true（可空）
 * 3. 如果类有 @NonNull/@NotNull 注解，返回 false（不可空）
 * 4. 如果类/包有 @NullMarked 且类没有 @NullUnmarked，返回 false（默认非空）
 * 5. 否则返回 true（保守策略：假设可空）
 *
 * 注意：如果需要检查字段/参数的可空性，请使用 Field.isNullable() 或 Parameter.isNullable()
 *
 * @return true 表示类型可为 null，false 表示类型不可为 null
 */
fun Class<*>.isNullable(): Boolean {
    // 1. 基本类型永远不可空
    if (this.isPrimitive) {
        return false
    }

    // 2. 显式 @Nullable 注解
    if (this.hasNullableAnnotation()) {
        return true
    }

    // 3. 显式 @NonNull/@NotNull 注解
    if (this.hasNonNullAnnotation()) {
        return false
    }

    // 4. JSpecify @NullMarked 上下文
    if (this.isNullMarkedContext()) {
        // 检查类是否被 @NullUnmarked 标记（覆盖上下文）
        if (!this.isNullUnmarkedContext()) {
            return false // 在 @NullMarked 上下文中且未被 @NullUnmarked 覆盖，默认非空
        }
    }

    // 5. 保守策略：假设可空
    return true
}

/**
 * 常见的可空性注解简称集合
 */
private val NULLABLE_ANNOTATION_SIMPLE_NAMES = setOf(
    "Nullable",           // JSpecify, JSR-305, JetBrains, Android, Eclipse, FindBugs
    "CheckForNull",       // JSR-305
)

/**
 * 常见的非空注解简称集合
 */
private val NON_NULL_ANNOTATION_SIMPLE_NAMES = setOf(
    "NonNull",            // JSpecify, JSR-305, Android, Eclipse, Spring, FindBugs
    "Nonnull",            // JSR-305
    "NotNull",            // JetBrains, Lombok
)

/**
 * JSpecify 的 @NullMarked 注解，表示默认非空
 */
private const val NULL_MARKED_ANNOTATION_SIMPLE_NAME = "NullMarked"

/**
 * JSpecify 的 @NullUnmarked 注解，表示不确定可空性
 */
private const val NULL_UNMARKED_ANNOTATION_SIMPLE_NAME = "NullUnmarked"

/**
 * 检查 Class 是否有可空注解
 */
private fun Class<*>.hasNullableAnnotation(): Boolean {
    return annotations.any { annotation ->
        val simpleName = annotation.annotationClass.java.simpleName
        simpleName in NULLABLE_ANNOTATION_SIMPLE_NAMES
    }
}

/**
 * 检查 Class 是否有非空注解
 */
private fun Class<*>.hasNonNullAnnotation(): Boolean {
    return annotations.any { annotation ->
        val simpleName = annotation.annotationClass.java.simpleName
        simpleName in NON_NULL_ANNOTATION_SIMPLE_NAMES
    }
}

/**
 * 检查类或包是否标记了 @NullMarked（JSpecify）
 */
private fun Class<*>.isNullMarkedContext(): Boolean {
    // 检查类本身
    if (annotations.any { it.annotationClass.java.simpleName == NULL_MARKED_ANNOTATION_SIMPLE_NAME }) {
        return true
    }

    // 检查包级别（package-info.java）
    val pkg = this.`package`
    if (pkg != null) {
        return pkg.annotations.any { it.annotationClass.java.simpleName == NULL_MARKED_ANNOTATION_SIMPLE_NAME }
    }

    return false
}

/**
 * 检查类或包是否标记了 @NullUnmarked（JSpecify）
 */
private fun Class<*>.isNullUnmarkedContext(): Boolean {
    // 检查类本身
    if (annotations.any { it.annotationClass.java.simpleName == NULL_UNMARKED_ANNOTATION_SIMPLE_NAME }) {
        return true
    }

    // 检查包级别
    val pkg = this.`package`
    if (pkg != null) {
        return pkg.annotations.any { it.annotationClass.java.simpleName == NULL_UNMARKED_ANNOTATION_SIMPLE_NAME }
    }

    return false
}

fun Class<*>.isPojo(): Boolean {
    return site.addzero.lsi.assist.checkIsPojo(
        isInterface = this.isInterface,
        isEnum = this.isEnum,
        isAbstract = Modifier.isAbstract(this.modifiers),
        isDataClass = false,
        annotationNames = this.annotations.map { it.annotationClass.java.name },
        isShortName = false
    )
}
