package site.addzero.util.lsi_impl.impl.psi.field

import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.assist.isNullable
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.type.LsiType
import site.addzero.util.lsi_impl.impl.psi.anno.PsiLsiAnnotation
import site.addzero.util.lsi_impl.impl.psi.clazz.PsiLsiClass
import site.addzero.util.lsi_impl.impl.psi.type.PsiLsiType
import site.addzero.util.lsi_impl.impl.psi.type.isPrimitiveType

/**
 * 基于 PSI 的 LsiField 实现
 *
 * 性能优化：使用 lazy 委托实现按需加载和缓存
 * - 轻量属性（name, typeName等）：直接计算
 * - 计算属性（comment, annotations等）：lazy加载
 * - 嵌套转换（declaringClass, fieldTypeClass等）：lazy加载，避免级联转换
 * - 递归属性（children）：lazy加载，避免深层递归开销
 */
class PsiLsiField(private val psiField: PsiField) : LsiField {

    // 基础属性：轻量级，直接计算
    override val name: String?
        get() = psiField.name

    override val typeName: String
        get() {
            val presentableText = psiField.type.presentableText
            return presentableText
        }

    // 类型转换：lazy加载（避免每次访问都创建新的LsiType对象）
    override val type: LsiType? by lazy {
        PsiLsiType(psiField.type)
    }

    // 注释提取：lazy加载（可能涉及PSI树遍历）
    override val comment: String? by lazy {
        psiField.getComment()
    }

    // 集合属性：lazy加载
    override val annotations: List<LsiAnnotation> by lazy {
        psiField.annotations.map { PsiLsiAnnotation(it) }
    }

    // 布尔属性：根据计算复杂度选择是否lazy
    override val isStatic: Boolean by lazy {
        psiField.isStaticField()
    }

    override val isConstant: Boolean by lazy {
        psiField.isConstantField()
    }

    override val isVar: Boolean
        get() = !psiField.hasModifierProperty(PsiModifier.FINAL)

    override val isLateInit: Boolean
        get() = false  // Java 不支持 lateinit

    override val isCollectionType: Boolean by lazy {
        psiField.isCollectionType()
    }

    // 字符串属性：lazy加载
    override val defaultValue: String? by lazy {
        psiField.initializer?.text
    }

    override val columnName: String? by lazy {
        psiField.getColumnName()
    }

    // 嵌套转换：lazy加载，避免级联转换开销
    override val declaringClass: LsiClass? by lazy {
        psiField.containingClass?.let { PsiLsiClass(it) }
    }

    override val fieldTypeClass: LsiClass? by lazy {
        when (val psiType = psiField.type) {
            is PsiClassType -> psiType.resolve()?.let { PsiLsiClass(it) }
            else -> null
        }
    }

    override val isNestedObject: Boolean by lazy {
        when (val psiType = psiField.type) {
            is PsiClassType -> {
                val psiClass = psiType.resolve()
                psiClass != null && !psiClass.isEnum && !psiClass.isInterface && !psiClass.qualifiedName.isNullOrEmpty()
            }
            else -> false
        }
    }

    // 递归属性：lazy加载，避免深层递归的性能开销
    override val children: List<LsiField> by lazy {
        when (val psiType = psiField.type) {
            is PsiClassType -> {
                val psiClass = psiType.resolve()
                if (psiClass != null && !psiClass.isEnum && !psiClass.isInterface) {
                    psiClass.allFields.map { PsiLsiField(it) }
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }

    // 可空性判断：基于 JSpecify @Nullable/@NonNull 注解
    override val isNullable: Boolean by lazy {
        val isPrimitive = psiField.type.isPrimitiveType()
        if (isPrimitive) return@lazy false

        return@lazy annotations.isNullable()
    }
}
