package site.addzero.lsi.kt.anno

import com.intellij.psi.PsiElement
import site.addzero.lsi.anno.LsiAnnotation
import org.jetbrains.kotlin.psi.KtAnnotationEntry

/**
 * 基于 Kotlin PSI 的 LsiAnnotation 实现
 * 支持两种构造方式：直接传入 KtAnnotationEntry 或使用反射
 */
class KtLsiAnnotation : LsiAnnotation {
    
    private val ktAnnotation: KtAnnotationEntry?
    private val psiElement: PsiElement?
    
    constructor(ktAnnotation: KtAnnotationEntry) {
        this.ktAnnotation = ktAnnotation
        this.psiElement = null
    }
    
    constructor(element: PsiElement, useReflection: Boolean) {
        if (useReflection) {
            this.ktAnnotation = null
            this.psiElement = element
        } else {
            @Suppress("UNCHECKED_CAST")
            this.ktAnnotation = element as KtAnnotationEntry
            this.psiElement = null
        }
    }
    
    override val qualifiedName: String?
        get() = ktAnnotation?.shortName?.asString() ?: getShortNameByReflection()

    override val simpleName: String?
        get() = ktAnnotation?.shortName?.asString() ?: getShortNameByReflection()

    override val attributes: Map<String, Any?> by lazy {
        ktAnnotation?.valueArguments?.associate { argument ->
            (argument.getArgumentName()?.asName?.asString() ?: "value") to argument.getArgumentExpression()?.text
        } ?: emptyMap()
    }

    override fun getAttribute(name: String): Any? {
        return ktAnnotation?.valueArguments?.find {
            it.getArgumentName()?.asName?.asString() == name
        }?.getArgumentExpression()?.text
    }

    override fun hasAttribute(name: String): Boolean {
        return ktAnnotation?.valueArguments?.any {
            it.getArgumentName()?.asName?.asString() == name
        } ?: false
    }
    
    private fun getShortNameByReflection(): String? {
        return psiElement?.let { element ->
            try {
                val shortName = element::class.java.getMethod("getShortName").invoke(element)
                shortName?.let { 
                    it::class.java.getMethod("asString").invoke(it) as? String 
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
