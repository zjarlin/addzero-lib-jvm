package site.addzero.lsi.jimmer

import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField

// ── 实体类型判断 ──────────────────────────────────────────────────────────────

/** 是否为任意 Jimmer 实体类型（@Entity / @MappedSuperclass / @Embeddable / @Immutable） */
val LsiClass.isJimmerType: Boolean
    get() = annotations.any { it.qualifiedName in ALL_JIMMER_ENTITY_ANNOTATIONS }

/** 是否为 @Entity */
val LsiClass.isJimmerEntity: Boolean
    get() = annotations.any { it.qualifiedName == ENTITY }

/** 是否为 @MappedSuperclass */
val LsiClass.isJimmerMappedSuperclass: Boolean
    get() = annotations.any { it.qualifiedName == MAPPED_SUPERCLASS }

/** 是否为 @Embeddable */
val LsiClass.isJimmerEmbeddable: Boolean
    get() = annotations.any { it.qualifiedName == EMBEDDABLE }

/** 是否为纯 @Immutable（无 SQL 语义） */
val LsiClass.isJimmerImmutable: Boolean
    get() = annotations.any { it.qualifiedName == IMMUTABLE }


// ── 微服务属性 ────────────────────────────────────────────────────────────────

/**
 * 读取 `@Entity(microServiceName=...)` 或 `@MappedSuperclass(microServiceName=...)` 的值。
 * 不存在时返回空字符串。
 */
val LsiClass.jimmerMicroServiceName: String
    get() = annotations
        .firstOrNull { it.qualifiedName == ENTITY || it.qualifiedName == MAPPED_SUPERCLASS }
        ?.getAttribute("microServiceName")?.toString()
        ?: ""

/**
 * 读取 `@MappedSuperclass(acrossMicroServices=...)` 的值。
 */
val LsiClass.isJimmerAcrossMicroServices: Boolean
    get() = annotations
        .firstOrNull { it.qualifiedName == MAPPED_SUPERCLASS }
        ?.getAttribute("acrossMicroServices")
        ?.toString()
        ?.toBooleanStrictOrNull()
        ?: false

// ── 属性快捷访问 ──────────────────────────────────────────────────────────────

/** 所有 Jimmer 属性（过滤掉静态/常量字段，保留抽象属性） */
val LsiClass.jimmerFields: List<LsiField>
    get() = fields.filter { !it.isStatic && !it.isConstant }

/** 主键属性（`@Id`），至多一个 */
val LsiClass.jimmerIdField: LsiField?
    get() = jimmerFields.firstOrNull { it.isJimmerId }

/** 版本号属性（`@Version`） */
val LsiClass.jimmerVersionField: LsiField?
    get() = jimmerFields.firstOrNull { it.isJimmerVersion }

/** 逻辑删除属性（`@LogicalDeleted`） */
val LsiClass.jimmerLogicalDeletedField: LsiField?
    get() = jimmerFields.firstOrNull { it.isJimmerLogicalDeleted }

/** 所有关联属性（`@ManyToOne` / `@OneToOne` / `@OneToMany` / `@ManyToMany`） */
val LsiClass.jimmerAssociationFields: List<LsiField>
    get() = jimmerFields.filter { it.isJimmerAssociation }

/** 所有标量属性（非关联、非 formula、非 transient） */
val LsiClass.jimmerScalarFields: List<LsiField>
    get() = jimmerFields.filter { !it.isJimmerAssociation && !it.isJimmerFormula && !it.isJimmerTransient }

/** 所有业务主键属性（`@Key`） */
val LsiClass.jimmerKeyFields: List<LsiField>
    get() = jimmerFields.filter { it.isJimmerKey }
