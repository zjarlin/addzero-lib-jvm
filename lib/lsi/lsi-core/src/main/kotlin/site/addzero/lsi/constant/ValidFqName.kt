package site.addzero.lsi.constant

import site.addzero.lsi.types.PojoAnnotationType
import site.addzero.lsi.types.PojoAnnotationCategory
import site.addzero.lsi.types.DocumentationAnnotationType
import site.addzero.lsi.types.ColumnAnnotationType
import site.addzero.lsi.types.CollectionType
import site.addzero.lsi.types.NullabilityAnnotationType

// ============ 向后兼容的常量定义 (从枚举生成) ============

@Deprecated(
    "Use PojoAnnotationType.JIMMER_ENTITY.fqName instead",
    ReplaceWith("PojoAnnotationType.JIMMER_ENTITY.fqName", "site.addzero.lsi.types.PojoAnnotationType")
)
const val JIMMER_ENTITY = "org.babyfish.jimmer.sql.Entity"

@Deprecated(
    "Use PojoAnnotationType.allInCategory(PojoAnnotationCategory.ENTITY).filter { it.fqName.contains(\"jimmer\") } instead",
    ReplaceWith("PojoAnnotationType.entries.filter { it.category == PojoAnnotationCategory.ENTITY && it.fqName.contains(\"jimmer\") }.map { it.fqName }.toSet()", "site.addzero.lsi.types.PojoAnnotationType", "site.addzero.lsi.types.PojoAnnotationCategory")
)
val JIMMER_POJO = setOf(
    PojoAnnotationType.JIMMER_ENTITY.fqName,
    "org.babyfish.jimmer.sql.MappedSuperclass",
)

@Deprecated(
    "Use PojoAnnotationType.allFqNames instead",
    ReplaceWith("PojoAnnotationType.allFqNames", "site.addzero.lsi.types.PojoAnnotationType")
)
val POJO_ANNOTATION get() = PojoAnnotationType.allFqNames

@Deprecated(
    "Use PojoAnnotationType.allEntityAnnotations.map { it.fqName }.toSet() instead",
    ReplaceWith("PojoAnnotationType.allEntityAnnotations.map { it.fqName }.toSet()", "site.addzero.lsi.types.PojoAnnotationType")
)
val ENTITY_ANNOTATIONS get() = PojoAnnotationType.allEntityAnnotations.map { it.fqName }.toSet()

@Deprecated(
    "Use PojoAnnotationType.allShortNames + PojoAnnotationType.allTableAnnotations.map { it.shortName } instead",
    ReplaceWith("(PojoAnnotationType.allEntityAnnotations + PojoAnnotationType.allTableAnnotations).map { it.shortName }.toSet().toList()", "site.addzero.lsi.types.PojoAnnotationType")
)
val SIMPLE_ENTITY_ANNOTATIONS = listOf(
    "Entity",
    "MappedSuperclass",
    "Table",
    "TableName",
)

@Deprecated(
    "Use PojoAnnotationType.allTableAnnotations.map { it.fqName }.toSet() instead",
    ReplaceWith("PojoAnnotationType.allTableAnnotations.map { it.fqName }.toSet()", "site.addzero.lsi.types.PojoAnnotationType")
)
val TABLE_ANNOTATIONS get() = PojoAnnotationType.allTableAnnotations.map { it.fqName }.toSet()

@Deprecated(
    "Use PojoAnnotationType.allLombokAnnotations.map { it.fqName }.toSet() instead",
    ReplaceWith("PojoAnnotationType.allLombokAnnotations.map { it.fqName }.toSet()", "site.addzero.lsi.types.PojoAnnotationType")
)
val LOMBOK_ANNOTATIONS get() = PojoAnnotationType.allLombokAnnotations.map { it.fqName }.toSet()

@Deprecated(
    "Use DocumentationAnnotationType.EXCEL_PROPERTY_ALIBABA.fqName instead",
    ReplaceWith("DocumentationAnnotationType.EXCEL_PROPERTY_ALIBABA.fqName", "site.addzero.lsi.types.DocumentationAnnotationType")
)
const val EXCEL_PROPERTY_ALIBABA = "com.alibaba.excel.annotation.ExcelProperty"

@Deprecated(
    "Use DocumentationAnnotationType.EXCEL_PROPERTY_IDEV.fqName instead",
    ReplaceWith("DocumentationAnnotationType.EXCEL_PROPERTY_IDEV.fqName", "site.addzero.lsi.types.DocumentationAnnotationType")
)
const val EXCEL_PROPERTY_IDEV = "cn.idev.excel.annotation.ExcelProperty"

@Deprecated(
    "Use DocumentationAnnotationType.EXCEL_EASYPOI.fqName instead",
    ReplaceWith("DocumentationAnnotationType.EXCEL_EASYPOI.fqName", "site.addzero.lsi.types.DocumentationAnnotationType")
)
const val EXCEL_EASYPOI = "cn.afterturn.easypoi.excel.annotation.Excel"

@Deprecated(
    "Use DocumentationAnnotationType.SWAGGER_V2_API_MODEL_PROPERTY.fqName instead",
    ReplaceWith("DocumentationAnnotationType.SWAGGER_V2_API_MODEL_PROPERTY.fqName", "site.addzero.lsi.types.DocumentationAnnotationType")
)
const val API_MODEL_PROPERTY = "io.swagger.annotations.ApiModelProperty"

@Deprecated(
    "Use DocumentationAnnotationType.SWAGGER_V3_SCHEMA.fqName instead",
    ReplaceWith("DocumentationAnnotationType.SWAGGER_V3_SCHEMA.fqName", "site.addzero.lsi.types.DocumentationAnnotationType")
)
const val SCHEMA = "io.swagger.v3.oas.annotations.media.Schema"

@Deprecated(
    "Use ColumnAnnotationType.MYBATIS_PLUS_TABLE_FIELD.fqName instead",
    ReplaceWith("ColumnAnnotationType.MYBATIS_PLUS_TABLE_FIELD.fqName", "site.addzero.lsi.types.ColumnAnnotationType")
)
const val COM_BAOMIDOU_MYBATISPLUS_ANNOTATION_TABLE_FIELD = "com.baomidou.mybatisplus.annotation.TableField"

@Deprecated(
    "Use PojoAnnotationType.JPA_ENTITY.fqName instead",
    ReplaceWith("PojoAnnotationType.JPA_ENTITY.fqName", "site.addzero.lsi.types.PojoAnnotationType")
)
const val ENTITY = "javax.persistence.Entity"

@Deprecated(
    "Use PojoAnnotationType.JPA_MAPPED_SUPERCLASS.fqName instead",
    ReplaceWith("PojoAnnotationType.JPA_MAPPED_SUPERCLASS.fqName", "site.addzero.lsi.types.PojoAnnotationType")
)
const val MAPPED_SUPERCLASS = "javax.persistence.MappedSuperclass"

@Deprecated(
    "Use ColumnAnnotationType.JIMMER_COLUMN.fqName instead",
    ReplaceWith("ColumnAnnotationType.JIMMER_COLUMN.fqName", "site.addzero.lsi.types.ColumnAnnotationType")
)
const val JIMMER_COLUMN = "org.babyfish.jimmer.sql.Column"

@Deprecated(
    "Use ColumnAnnotationType.MYBATIS_PLUS_TABLE_FIELD.fqName instead",
    ReplaceWith("ColumnAnnotationType.MYBATIS_PLUS_TABLE_FIELD.fqName", "site.addzero.lsi.types.ColumnAnnotationType")
)
const val MP_TABLE_FIELD = "com.baomidou.mybatisplus.annotation.TableField"

// 集合类型的全限定名列表
@Deprecated(
    "Use CollectionType to check collection types instead",
    ReplaceWith("CollectionType.isCollection(typeName)", "site.addzero.lsi.types.CollectionType")
)
val COLLECTION_TYPE_FQ_NAMES = setOf(
    "java.util.Collection", "java.util.List", "java.util.Set", "java.util.Map",
    "kotlin.collections.Collection", "kotlin.collections.List", "kotlin.collections.Set",
    "kotlin.collections.Map", "kotlin.collections.ArrayList", "kotlin.collections.LinkedList",
    "kotlin.collections.HashSet", "kotlin.collections.LinkedHashSet",
    "kotlin.collections.HashMap", "kotlin.collections.LinkedHashMap"
)


// 定义注解与对应方法名的映射关系
@Deprecated(
    "Use DocumentationAnnotationType.fqNameToAttributeMap instead",
    ReplaceWith("DocumentationAnnotationType.fqNameToAttributeMap", "site.addzero.lsi.types.DocumentationAnnotationType")
)
val COMMENT_ANNOTATION_METHOD_MAP get() = DocumentationAnnotationType.fqNameToAttributeMap

@Deprecated(
    "Use ColumnAnnotationType.fqNameToAttributeMap instead",
    ReplaceWith("ColumnAnnotationType.fqNameToAttributeMap", "site.addzero.lsi.types.ColumnAnnotationType")
)
val COLUMN_NAME_ANNOTATION_METHOD_MAP get() = ColumnAnnotationType.fqNameToAttributeMap

@Deprecated(
    "Use ColumnAnnotationType.allFqNames instead",
    ReplaceWith("ColumnAnnotationType.allFqNames", "site.addzero.lsi.types.ColumnAnnotationType")
)
val COLUMN_ANNOTATIONS get() = ColumnAnnotationType.allFqNames

@Deprecated(
    "Use NullabilityAnnotationType.allNullableSimpleNames instead",
    ReplaceWith("NullabilityAnnotationType.allNullableSimpleNames", "site.addzero.lsi.types.NullabilityAnnotationType")
)
val NULLABLE_ANNOTATION_SIMPLE_NAMES get() = NullabilityAnnotationType.allNullableSimpleNames

@Deprecated(
    "Use NullabilityAnnotationType.allNonNullSimpleNames instead",
    ReplaceWith("NullabilityAnnotationType.allNonNullSimpleNames", "site.addzero.lsi.types.NullabilityAnnotationType")
)
val NON_NULL_ANNOTATION_SIMPLE_NAMES get() = NullabilityAnnotationType.allNonNullSimpleNames

@Deprecated(
    "Use NullabilityAnnotationType.NULL_MARKED.simpleName instead",
    ReplaceWith("NullabilityAnnotationType.NULL_MARKED.simpleName", "site.addzero.lsi.types.NullabilityAnnotationType")
)
const val NULL_MARKED_ANNOTATION_SIMPLE_NAME = "NullMarked"

@Deprecated(
    "Use NullabilityAnnotationType.NULL_UNMARKED.simpleName instead",
    ReplaceWith("NullabilityAnnotationType.NULL_UNMARKED.simpleName", "site.addzero.lsi.types.NullabilityAnnotationType")
)
const val NULL_UNMARKED_ANNOTATION_SIMPLE_NAME = "NullUnmarked"
