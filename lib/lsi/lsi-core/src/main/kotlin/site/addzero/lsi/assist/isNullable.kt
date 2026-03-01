package site.addzero.lsi.assist

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.types.Nullability
import site.addzero.lsi.types.NullabilityAnnotationType

fun List<LsiAnnotation>.hasNullableAnnotation(): Boolean =
    any { it.simpleName?.let { sn -> NullabilityAnnotationType.findBySimpleName(sn)?.nullability == Nullability.NULLABLE } == true }

fun List<LsiAnnotation>.hasNonNullAnnotation(): Boolean =
    any { it.simpleName?.let { sn -> NullabilityAnnotationType.findBySimpleName(sn)?.nullability == Nullability.NON_NULL } == true }

fun List<LsiAnnotation>.isNullable(): Boolean {
    if (hasNullableAnnotation()) return true
    if (hasNonNullAnnotation()) return false
    return true
}
