package site.addzero.lsi.jimmer

import site.addzero.lsi.field.LsiField

// ── 标量语义标志 ──────────────────────────────────────────────────────────────

/** 是否为主键属性（`@Id`） */
val LsiField.isJimmerId: Boolean
    get() = annotations.any { it.qualifiedName == ID }

/** 是否为版本号属性（`@Version`） */
val LsiField.isJimmerVersion: Boolean
    get() = annotations.any { it.qualifiedName == VERSION }

/** 是否为逻辑删除属性（`@LogicalDeleted`） */
val LsiField.isJimmerLogicalDeleted: Boolean
    get() = annotations.any { it.qualifiedName == LOGICAL_DELETED }

/** 是否为业务主键属性（`@Key`） */
val LsiField.isJimmerKey: Boolean
    get() = annotations.any { it.qualifiedName == KEY }

/** 是否为公式属性（`@Formula`） */
val LsiField.isJimmerFormula: Boolean
    get() = annotations.any { it.qualifiedName == FORMULA }

/** 是否为瞬态属性（`@Transient`） */
val LsiField.isJimmerTransient: Boolean
    get() = annotations.any { it.qualifiedName == TRANSIENT }

// ── 关联语义标志 ──────────────────────────────────────────────────────────────

/** 是否为 @ManyToOne */
val LsiField.isJimmerManyToOne: Boolean
    get() = annotations.any { it.qualifiedName == MANY_TO_ONE }

/** 是否为 @OneToOne */
val LsiField.isJimmerOneToOne: Boolean
    get() = annotations.any { it.qualifiedName == ONE_TO_ONE }

/** 是否为 @OneToMany */
val LsiField.isJimmerOneToMany: Boolean
    get() = annotations.any { it.qualifiedName == ONE_TO_MANY }

/** 是否为 @ManyToMany */
val LsiField.isJimmerManyToMany: Boolean
    get() = annotations.any { it.qualifiedName == MANY_TO_MANY }

/** 是否为任意关联属性 */
val LsiField.isJimmerAssociation: Boolean
    get() = annotations.any { it.qualifiedName in ALL_JIMMER_ASSOCIATION_ANNOTATIONS }

/** 是否为反向关联（`mappedBy` 非空） */
val LsiField.isJimmerReverse: Boolean
    get() = annotations
        .filter { it.qualifiedName in ALL_JIMMER_ASSOCIATION_ANNOTATIONS }
        .any { anno ->
            val v = anno.getAttribute("mappedBy")?.toString()
            !v.isNullOrEmpty()
        }

/** 读取 `mappedBy` 值；非反向关联返回 `null` */
val LsiField.jimmerMappedBy: String?
    get() = annotations
        .filter { it.qualifiedName in ALL_JIMMER_ASSOCIATION_ANNOTATIONS }
        .firstNotNullOfOrNull { anno ->
            anno.getAttribute("mappedBy")?.toString()?.takeIf { it.isNotEmpty() }
        }

/** 是否为 @IdView */
val LsiField.isJimmerIdView: Boolean
    get() = annotations.any { it.qualifiedName == ID_VIEW }

/** 是否为 @ManyToManyView */
val LsiField.isJimmerManyToManyView: Boolean
    get() = annotations.any { it.qualifiedName == MANY_TO_MANY_VIEW }

// ── 便捷别名 ──────────────────────────────────────────────────────────────────

/** 与 [PropKind] 对齐的 `isId` 别名 */
val LsiField.isId: Boolean get() = isJimmerId

/** 与 [PropKind] 对齐的 `isVersion` 别名 */
val LsiField.isVersion: Boolean get() = isJimmerVersion

/** 与 [PropKind] 对齐的 `isLogicalDeleted` 别名 */
val LsiField.isLogicalDeleted: Boolean get() = isJimmerLogicalDeleted

/** 与 [PropKind] 对齐的 `isAssociation` 别名 */
val LsiField.isAssociation: Boolean get() = isJimmerAssociation
