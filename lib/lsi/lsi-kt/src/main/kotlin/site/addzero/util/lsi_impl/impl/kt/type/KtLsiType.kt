package site.addzero.util.lsi_impl.impl.kt.type

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtTypeReference
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.assist.TypeChecker
import site.addzero.util.lsi.assist.isArray
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.type.LsiType
import site.addzero.util.lsi_impl.impl.kt.anno.KtLsiAnnotation

/**
 * 基于 Kotlin PSI 的 LsiType 实现
 * 支持两种构造方式：直接传入 KtTypeReference 或使用反射
 */
class KtLsiType : LsiType {

    private val ktType: KtTypeReference?
    private val psiElement: PsiElement?

    constructor(ktType: KtTypeReference) {
        this.ktType = ktType
        this.psiElement = null
    }

    constructor(element: PsiElement, useReflection: Boolean) {
        if (useReflection) {
            this.ktType = null
            this.psiElement = element
        } else {
            @Suppress("UNCHECKED_CAST")
            this.ktType = element as KtTypeReference
            this.psiElement = null
        }
    }

    override val name: String?
        get() = ktType?.text ?: invokeMethod("getText") as? String

    override val qualifiedName: String?
        get() = ktType?.name ?: invokeMethod("getName") as? String

    override val presentableText: String?
        get() = ktType?.text ?: invokeMethod("getText") as? String

    override val annotations: List<LsiAnnotation>
        get() = ktType?.annotationEntries?.map { KtLsiAnnotation(it) } ?: emptyList()

    override val isCollectionType: Boolean
        get() = ktType?.isCollectionType() ?: false

    override val isNullable: Boolean
        get() = ktType?.typeElement?.text?.endsWith("?") ?: (name?.endsWith("?") ?: false)

    override val typeParameters: List<LsiType>
        get() = ktType?.typeElement?.typeArgumentsAsTypes?.mapNotNull { it?.let { type -> KtLsiType(type) } } ?: emptyList()

    override val isPrimitive: Boolean
        get() = name?.let { TypeChecker.isKotlinPrimitiveType(it) } ?: false

    override val componentType: LsiType?
        get() = null

    override val isArray: Boolean
        get() = name?.isArray() ?: false

    override val lsiClass: LsiClass?
        get() = null

    private fun invokeMethod(methodName: String): Any? {
        return psiElement?.let { element ->
            try {
                element::class.java.getMethod(methodName).invoke(element)
            } catch (e: Exception) {
                null
            }
        }
    }
}
