package site.addzero.lsi.assist

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.types.ColumnAnnotationType

fun List<LsiAnnotation>.getColumnName(): String? =
    firstOrNull { it.qualifiedName?.let { qn -> ColumnAnnotationType.findByFqName(qn) } != null }
        ?.let { anno ->
            val qualifiedName = anno.qualifiedName ?: return@let null
            val columnType = ColumnAnnotationType.findByFqName(qualifiedName) ?: return@let null
            anno.getAttribute(columnType.nameAttribute)?.toString()?.takeIf { it.isNotBlank() }
        }

fun List<LsiAnnotation>.hasColumnAnno(): Boolean =
    any { it.qualifiedName?.let { qn -> ColumnAnnotationType.findByFqName(qn) } != null }
