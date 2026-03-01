package site.addzero.lsi.psi.anno

import site.addzero.lsi.anno.LsiAnnotation
import com.intellij.psi.PsiAnnotation

/**
 * 基于 PSI 的 LsiAnnotation 实现
 * 
 * 性能优化：使用 lazy 委托
 * - qualifiedName, simpleName：轻量级，直接计算
 * - attributes：lazy加载（需要遍历所有属性）
 * - getAttribute/hasAttribute：按需查询，不缓存
 */
class PsiLsiAnnotation(private val psiAnnotation: PsiAnnotation) : LsiAnnotation {
    
    // 基础属性：轻量级，直接计算
    override val qualifiedName: String?
        get() = psiAnnotation.qualifiedName

    override val simpleName: String?
        get() = psiAnnotation.nameReferenceElement?.referenceName

    // 集合属性：lazy加载（需要遍历所有属性）
    override val attributes: Map<String, Any?> by lazy {
        psiAnnotation.parameterList?.attributes?.associate { attribute ->
            val name = attribute.name ?: "value"
            val value = attribute.value?.text
            name to value
        } ?: emptyMap()
    }

    // 方法：按需查询，不缓存（灵活性优先）
    override fun getAttribute(name: String): Any? {
        return psiAnnotation.findAttributeValue(name)?.text
    }

    override fun hasAttribute(name: String): Boolean {
        return psiAnnotation.findAttributeValue(name) != null
    }
}
