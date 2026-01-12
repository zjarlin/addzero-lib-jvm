package site.addzero.util.lsi_impl.impl.kt.`fun`

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtParameter
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.method.LsiMethod
import site.addzero.util.lsi.method.LsiParameter
import site.addzero.util.lsi.type.LsiType
import site.addzero.util.lsi_impl.impl.kt.anno.KtLsiAnnotation
import site.addzero.util.lsi_impl.impl.kt.clazz.KtLsiClass
import site.addzero.util.lsi_impl.impl.kt.type.KtLsiType

/**
 * 基于 Kotlin PSI 的 LsiMethod 实现
 * 支持两种构造方式：直接传入 KtFunction 或使用反射
 */
class KtLsiMethod : LsiMethod {
    
    private val ktFunction: KtFunction?
    private val psiElement: PsiElement?
    
    constructor(ktFunction: KtFunction) {
        this.ktFunction = ktFunction
        this.psiElement = null
    }
    
    constructor(element: PsiElement, useReflection: Boolean) {
        if (useReflection) {
            this.ktFunction = null
            this.psiElement = element
        } else {
            @Suppress("UNCHECKED_CAST")
            this.ktFunction = element as KtFunction
            this.psiElement = null
        }
    }
    
    override val name: String?
        get() = ktFunction?.name ?: invokeMethod("getName") as? String

    override val returnTypeName: String?
        get() = ktFunction?.typeReference?.text ?: run {
            val typeRef = invokeMethod("getTypeReference")
            typeRef?.let { invokeMethodOn(it, "getText") as? String }
        }

    override val returnType: LsiType? by lazy {
        ktFunction?.typeReference?.let { KtLsiType(it) }
    }

    override val comment: String? by lazy {
        ktFunction?.getComment()
    }

    override val annotations: List<LsiAnnotation> by lazy {
        ktFunction?.annotationEntries?.map { KtLsiAnnotation(it) } ?: emptyList()
    }

    override val parameters: List<LsiParameter> by lazy {
        ktFunction?.valueParameters?.map { KtLsiParameter(it) } ?: emptyList()
    }

    override val isStatic: Boolean by lazy {
        ktFunction?.isStaticFunction() ?: false
    }

    override val isAbstract: Boolean
        get() = ktFunction?.hasModifier(KtTokens.ABSTRACT_KEYWORD) ?: false

    override val declaringClass: LsiClass? by lazy {
        ktFunction?.let {
            val parent = it.parent
            if (parent is KtClass) KtLsiClass(parent) else null
        }
    }
    
    private fun invokeMethod(methodName: String): Any? {
        return psiElement?.let { element ->
            try {
                element::class.java.getMethod(methodName).invoke(element)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun invokeMethodOn(obj: Any, methodName: String): Any? {
        return try {
            obj::class.java.getMethod(methodName).invoke(obj)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 基于 Kotlin PSI 的 LsiParameter 实现
 * 
 * 性能优化：使用 lazy 委托
 * - name, typeName：轻量级，直接计算
 * - type, annotations：lazy加载
 */
class KtLsiParameter(private val ktParameter: KtParameter) : LsiParameter {
    
    // 基础属性：轻量级，直接计算
    override val name: String?
        get() = ktParameter.name

    override val typeName: String?
        get() = ktParameter.typeReference?.text

    // 类型转换：lazy加载
    override val type: LsiType? by lazy {
        ktParameter.typeReference?.let { KtLsiType(it) }
    }

    // 集合属性：lazy加载
    override val annotations: List<LsiAnnotation> by lazy {
        ktParameter.annotationEntries.map { KtLsiAnnotation(it) }
    }
}

