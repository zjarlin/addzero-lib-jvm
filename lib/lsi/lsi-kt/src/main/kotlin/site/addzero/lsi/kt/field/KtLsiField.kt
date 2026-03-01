package site.addzero.lsi.kt.field

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtProperty
import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.assist.isNullable
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.type.LsiType
import site.addzero.lsi.kt.anno.KtLsiAnnotation
import site.addzero.lsi.kt.clazz.KtLsiClass
import site.addzero.lsi.kt.type.KtLsiType

/**
 * 基于 Kotlin PSI 的 LsiField 实现
 */
class KtLsiField(private val ktProperty: KtProperty) : LsiField {
    override val name: String?
        get() = ktProperty.name

    override val type: LsiType?
        get() = ktProperty.typeReference?.let { KtLsiType(it) }

    override val typeName: String?
        get() {
            val typeReference = ktProperty.typeReference
            return typeReference?.text
        }

    override val comment: String?
        get() = ktProperty.getComment()

    override val annotations: List<LsiAnnotation>
        get() = ktProperty.annotationEntries.map { KtLsiAnnotation(it) }

    override val isStatic: Boolean
        get() = ktProperty.isStaticField()

    override val isConstant: Boolean
        get() = ktProperty.hasModifier(KtTokens.CONST_KEYWORD)

    override val isVar: Boolean
        get() = ktProperty.isVar

    override val isLateInit: Boolean
        get() = ktProperty.hasModifier(KtTokens.LATEINIT_KEYWORD)

    override val isCollectionType: Boolean
        get() = ktProperty.isCollectionType()

    override val defaultValue: String?
        get() {
            val text = ktProperty.initializer?.text
            return text
        }

    override val columnName: String?
        get() = ktProperty.getColumnName()

    // 新增属性的实现

    override val declaringClass: LsiClass?
        get() = (ktProperty.parent as? org.jetbrains.kotlin.psi.KtClass)
            ?.let { KtLsiClass(it) }

    override val fieldTypeClass: LsiClass?
        get() = null // Kotlin PSI中类型解析需要BindingContext，暂不实现

    override val isNestedObject: Boolean
        get() = false // 简化实现，避免复杂的类型解析

    override val children: List<LsiField>
        get() = emptyList() // 简化实现，避免递归字段解析

    /**
     * 判断字段是否可空
     */
    override val isNullable: Boolean
        get() {
            // 检查类型是否可空（Kotlin 特有的 ? 后缀）
            val typeText = ktProperty.typeReference?.text
            if (typeText?.endsWith('?') == true) return true
            val nullable = annotations.isNullable()
            return nullable
        }



}
