package site.addzero.util.lsi_impl.impl.psi.type

import com.intellij.psi.*
import com.intellij.psi.util.PsiUtil
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.type.LsiType
import site.addzero.util.lsi_impl.impl.psi.anno.PsiLsiAnnotation
import site.addzero.util.lsi_impl.impl.psi.clazz.PsiLsiClass

/**
 * 基于 PSI 的 LsiType 实现
 * 
 * 性能优化：使用 lazy 委托
 * - name, presentableText：轻量级，直接计算
 * - qualifiedName：lazy加载（需要resolve）
 * - annotations, typeParameters：lazy加载（集合转换）
 * - lsiClass：lazy加载（复杂的泛型解析）
 * - isPrimitive, isArray：轻量级类型检查，直接计算
 */
class PsiLsiType(private val psiType: PsiType) : LsiType {

    // 基础属性：轻量级，直接计算
    override val name: String?
        get() = psiType.presentableText

    override val presentableText: String?
        get() = psiType.presentableText

    // Resolve操作：lazy加载
    override val qualifiedName: String? by lazy {
        when (psiType) {
            is PsiClassType -> psiType.resolve()?.qualifiedName
            else -> null
        }
    }

    // 集合属性：lazy加载
    override val annotations: List<LsiAnnotation> by lazy {
        psiType.annotations.map { PsiLsiAnnotation(it) }
    }

    override val typeParameters: List<LsiType> by lazy {
        when (psiType) {
            is PsiClassType -> psiType.parameters.map { PsiLsiType(it) }
            else -> emptyList()
        }
    }

    // 布尔属性：根据计算复杂度选择
    override val isCollectionType: Boolean by lazy {
        psiType.isCollectionType()
    }

    override val isPrimitive: Boolean
        get() = psiType is PsiPrimitiveType

    override val isArray: Boolean
        get() = psiType is PsiArrayType

    // 类型转换：lazy加载
    override val componentType: LsiType? by lazy {
        when (psiType) {
            is PsiArrayType -> PsiLsiType(psiType.componentType)
            else -> null
        }
    }

    // 复杂解析：lazy加载（涉及泛型解析）
    override val lsiClass: LsiClass? by lazy {
        val generic = PsiUtil.resolveGenericsClassInType(this.psiType)
        if (generic.substitutor == PsiSubstitutor.EMPTY) {
            generic.element?.let { PsiLsiClass(it) }
        } else {
            val propTypeParameters = generic.element?.typeParameters ?: return@lazy null
            generic.substitutor.substitute(propTypeParameters[0])?.let { it ->
                PsiUtil.resolveClassInType(it)?.let { PsiLsiClass(it) }
            }
        }
    }
}
