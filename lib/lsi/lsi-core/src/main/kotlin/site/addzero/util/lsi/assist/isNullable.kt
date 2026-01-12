package site.addzero.util.lsi.assist

import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.types.Nullability
import site.addzero.util.lsi.types.NullabilityAnnotationType

fun List<LsiAnnotation>.hasNullableAnnotation(): Boolean =
    any { it.simpleName?.let { sn -> NullabilityAnnotationType.findBySimpleName(sn)?.nullability == Nullability.NULLABLE } == true }

fun List<LsiAnnotation>.hasNonNullAnnotation(): Boolean =
    any { it.simpleName?.let { sn -> NullabilityAnnotationType.findBySimpleName(sn)?.nullability == Nullability.NON_NULL } == true }

fun List<LsiAnnotation>.isNullable(): Boolean {
    if (hasNullableAnnotation()) return true
    if (hasNonNullAnnotation()) return false
    return true
}
