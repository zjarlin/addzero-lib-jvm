package site.addzero.util.lsi_impl.impl.psi.clazz

import com.intellij.psi.PsiClass
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.method.LsiMethod
import site.addzero.util.lsi_impl.impl.psi.anno.PsiLsiAnnotation
import site.addzero.util.lsi_impl.impl.psi.field.PsiLsiField
import site.addzero.util.lsi_impl.impl.psi.method.PsiLsiMethod

/**
 * 基于 PSI 的 LsiClass 实现
 *
 * 性能优化：使用 lazy 委托实现按需加载和缓存
 * - 轻量属性（name, qualifiedName等）：直接计算，开销小
 * - 集合属性（fields, methods等）：lazy加载，首次访问时转换并缓存
 * - 嵌套转换（superClasses等）：lazy加载，避免级联转换开销
 */
class PsiLsiClass(private val psiClass: PsiClass) : LsiClass {

    // 基础属性：轻量级，直接计算（无需缓存）
    override val name: String?
        get() = psiClass.name

    override val qualifiedName: String?
        get() = psiClass.qualifiedName

    override val comment: String? by lazy {
        psiClass.docComment?.text
    }

    // 集合属性：使用 lazy 延迟加载和缓存
    override val fields: List<LsiField> by lazy {
        psiClass.allFields.map { PsiLsiField(it) }
    }

    override val annotations: List<LsiAnnotation> by lazy {
        psiClass.annotations.map { PsiLsiAnnotation(it) }
    }

    //todo 错误用法
    override val methods: List<LsiMethod> by lazy {
        psiClass.getJimmerFields().map { PsiLsiMethod(it) }
    }

    // 布尔属性：轻量级，直接计算
    override val isInterface: Boolean
        get() = psiClass.isInterface

    override val isEnum: Boolean
        get() = psiClass.isEnum

    override val isCollectionType: Boolean by lazy {
        psiClass.isCollectionType()
    }

    override val isPojo: Boolean by lazy {
        psiClass.isPojo()
    }

    // 嵌套转换：使用 lazy 避免级联转换开销
    override val superClasses: List<LsiClass> by lazy {
        val supers = psiClass.supers
        val toList = supers.toList()
        val map = supers.map { it.supers.toList() }.flatten()
        val classes = toList + map
        classes.map { PsiLsiClass(it) }
    }

    override val interfaces: List<LsiClass> by lazy {
        psiClass.interfaces.map { PsiLsiClass(it) }
    }
}
