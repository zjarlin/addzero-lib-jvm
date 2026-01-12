package site.addzero.util.lsi_impl.impl.reflection.field

import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Parameter

/**
 * 常见的可空性注解简称集合
 *
 * 包括：
 * - JSpecify: @Nullable
 * - JSR-305: @Nullable, @CheckForNull
 * - JetBrains: @Nullable
 * - Android: @Nullable
 * - Eclipse: @Nullable
 * - Spring: @Nullable
 * - FindBugs: @Nullable
 */
private val NULLABLE_ANNOTATION_SIMPLE_NAMES = setOf(
    "Nullable",           // JSpecify, JSR-305, JetBrains, Android, Eclipse, FindBugs
    "CheckForNull",       // JSR-305
)

/**
 * 常见的非空注解简称集合
 *
 * 包括：
 * - JSpecify: @NonNull
 * - JSR-305: @Nonnull, @NonNull
 * - JetBrains: @NotNull
 * - Android: @NonNull
 * - Eclipse: @NonNull
 * - Spring: @NonNull
 * - Lombok: @NonNull
 * - FindBugs: @NonNull
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
 * 检查 AnnotatedElement 是否有可空注解
 */
private fun AnnotatedElement.hasNullableAnnotation(): Boolean {
    return annotations.any { annotation ->
        val simpleName = annotation.annotationClass.java.simpleName
        simpleName in NULLABLE_ANNOTATION_SIMPLE_NAMES
    }
}

/**
 * 检查 AnnotatedElement 是否有非空注解
 */
private fun AnnotatedElement.hasNonNullAnnotation(): Boolean {
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

/**
 * 判断字段是否可空
 *
 * 判断逻辑（按优先级）：
 * 1. 如果字段类型是基本类型（int, long, boolean 等），返回 false（不可空）
 * 2. 如果字段有 @Nullable 注解，返回 true（可空）
 * 3. 如果字段有 @NonNull/@NotNull 注解，返回 false（不可空）
 * 4. 如果所在类/包有 @NullMarked 且字段没有 @NullUnmarked，返回 false（默认非空）
 * 5. 否则返回 true（保守策略：假设可空）
 *
 * @return true 表示字段可为 null，false 表示字段不可为 null
 */
fun Field.isNullable(): Boolean {
    // 1. 基本类型永远不可空
    if (this.type.isPrimitive) {
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
    val declaringClass = this.declaringClass
    if (declaringClass.isNullMarkedContext()) {
        // 检查字段是否被 @NullUnmarked 标记（覆盖上下文）
        val isUnmarked = annotations.any {
            it.annotationClass.java.simpleName == NULL_UNMARKED_ANNOTATION_SIMPLE_NAME
        }
        if (!isUnmarked) {
            return false // 在 @NullMarked 上下文中且未被 @NullUnmarked 覆盖，默认非空
        }
    }

    // 5. 保守策略：假设可空
    return true
}

/**
 * 判断参数是否可空
 *
 * 判断逻辑与 Field.isNullable() 相同
 *
 * @return true 表示参数可为 null，false 表示参数不可为 null
 */
fun Parameter.isNullable(): Boolean {
    // 1. 基本类型永远不可空
    if (this.type.isPrimitive) {
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
    // 4. JSpecify @NullMarked 上下文（检查声明类）
    val declaringExecutable = this.declaringExecutable
    val declaringClass = declaringExecutable.declaringClass
    if (declaringClass.isNullMarkedContext()) {
        return false // 在 @NullMarked 上下文中，默认非空
    }
    // 5. 保守策略：假设可空
    return true
}
