package site.addzero.lsi.psi.method

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.method.LsiMethod
import site.addzero.lsi.method.LsiParameter
import site.addzero.lsi.type.LsiType
import site.addzero.lsi.psi.anno.PsiLsiAnnotation
import site.addzero.lsi.psi.clazz.PsiLsiClass
import site.addzero.lsi.psi.type.PsiLsiType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiParameter

/**
 * 基于 PSI 的 LsiMethod 实现
 * 
 * 性能优化：使用 lazy 委托
 * - name, returnTypeName：轻量级，直接计算
 * - returnType：lazy加载（类型转换）
 * - comment, annotations, parameters：lazy加载
 * - declaringClass：lazy加载（嵌套转换）
 */
class PsiLsiMethod(private val psiMethod: PsiMethod) : LsiMethod {
    
    // 基础属性：轻量级，直接计算
    override val name: String?
        get() = psiMethod.name

    override val returnTypeName: String?
        get() = psiMethod.returnType?.presentableText

    // 类型转换：lazy加载
    override val returnType: LsiType? by lazy {
        psiMethod.returnType?.let { PsiLsiType(it) }
    }

    // 注释：lazy加载
    override val comment: String? by lazy {
        psiMethod.getComment()
    }

    // 集合属性：lazy加载
    override val annotations: List<LsiAnnotation> by lazy {
        psiMethod.annotations.map { PsiLsiAnnotation(it) }
    }

    override val parameters: List<LsiParameter> by lazy {
        psiMethod.parameterList.parameters.map { PsiLsiParameter(it) }
    }

    // 布尔属性：轻量级，直接计算
    override val isStatic: Boolean
        get() = psiMethod.hasModifierProperty(PsiModifier.STATIC)

    override val isAbstract: Boolean
        get() = psiMethod.hasModifierProperty(PsiModifier.ABSTRACT)

    // 嵌套转换：lazy加载
    override val declaringClass: LsiClass? by lazy {
        psiMethod.containingClass?.let { PsiLsiClass(it) }
    }
}

/**
 * 基于 PSI 的 LsiParameter 实现
 * 
 * 性能优化：使用 lazy 委托
 * - name, typeName：轻量级，直接计算
 * - type, annotations：lazy加载
 */
class PsiLsiParameter(private val psiParameter: PsiParameter) : LsiParameter {
    
    // 基础属性：轻量级，直接计算
    override val name: String?
        get() = psiParameter.name

    override val typeName: String?
        get() = psiParameter.type.presentableText

    // 类型转换：lazy加载
    override val type: LsiType? by lazy {
        PsiLsiType(psiParameter.type)
    }

    // 集合属性：lazy加载
    override val annotations: List<LsiAnnotation> by lazy {
        psiParameter.annotations.map { PsiLsiAnnotation(it) }
    }
}

