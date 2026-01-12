package site.addzero.util.lsi_impl.impl.kt.clazz

import com.intellij.psi.PsiElement
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.method.LsiMethod
import site.addzero.util.lsi_impl.impl.kt.anno.KtLsiAnnotation
import site.addzero.util.lsi_impl.impl.kt.field.KtLsiField
import site.addzero.util.lsi_impl.impl.kt.`fun`.KtLsiMethod
import site.addzero.util.str.toUnderLineCase
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction

/**
 * 基于 Kotlin PSI 的 LsiClass 实现
 * 支持两种构造方式：
 * 1. 直接传入 KtClass（同一类加载器）
 * 2. 传入 PsiElement 并使用反射（跨类加载器）
 */
class KtLsiClass : LsiClass {
    
    private val ktClass: KtClass?
    private val psiElement: PsiElement?
    
    // 直接构造（同一类加载器）
    constructor(ktClass: KtClass) {
        this.ktClass = ktClass
        this.psiElement = null
    }
    
    // 反射构造（跨类加载器）
    constructor(element: PsiElement, useReflection: Boolean) {
        if (useReflection) {
            this.ktClass = null
            this.psiElement = element
        } else {
            @Suppress("UNCHECKED_CAST")
            this.ktClass = element as KtClass
            this.psiElement = null
        }
    }

    override val name: String?
        get() = ktClass?.name ?: invokeMethod("getName") as? String

    override val qualifiedName: String?
        get() = ktClass?.fqName?.asString() ?: run {
            val fqName = invokeMethod("getFqName")
            fqName?.let { invokeMethodOn(it, "asString") as? String }
        }

    override val comment: String?
        get() = ktClass?.docComment?.text ?: run {
            val docComment = invokeMethod("getDocComment")
            docComment?.let { invokeMethodOn(it, "getText") as? String }
        }

    override val fields: List<LsiField>
        get() = ktClass?.getProperties()?.map { KtLsiField(it) } ?: run {
            @Suppress("UNCHECKED_CAST")
            val properties = invokeMethod("getProperties") as? List<PsiElement> ?: emptyList()
            properties.mapNotNull { element ->
                try {
                    KtLsiField(element as org.jetbrains.kotlin.psi.KtProperty)
                } catch (e: Exception) {
                    null
                }
            }
        }

    override val annotations: List<LsiAnnotation>
        get() = ktClass?.annotationEntries?.map { KtLsiAnnotation(it) } ?: run {
            @Suppress("UNCHECKED_CAST")
            val entries = invokeMethod("getAnnotationEntries") as? List<PsiElement> ?: emptyList()
            entries.map { KtLsiAnnotation(it, true) }
        }

    override val isInterface: Boolean
        get() = ktClass?.isInterface() ?: (invokeMethod("isInterface") as? Boolean ?: false)

    override val isEnum: Boolean
        get() = ktClass?.isEnum() ?: (invokeMethod("isEnum") as? Boolean ?: false)

    override val isCollectionType: Boolean
        get() = ktClass?.isCollectionType() ?: false

    override val isPojo: Boolean
        get() = ktClass?.isPojo() ?: checkIsPojoByReflection()

    val guessTableName: String
        get() = ktClass?.guessTableEnglishName() ?: (name?.toUnderLineCase() ?: "")

    override val superClasses: List<LsiClass>
        get() = emptyList() // 简化实现

    override val interfaces: List<LsiClass>
        get() = emptyList() // 简化实现

    override val methods: List<LsiMethod>
        get() = ktClass?.declarations?.filterIsInstance<KtFunction>()?.map { KtLsiMethod(it) } ?: run {
            @Suppress("UNCHECKED_CAST")
            val declarations = invokeMethod("getDeclarations") as? List<PsiElement> ?: emptyList()
            declarations.filter { 
                it::class.java.name.endsWith(".KtFunction") || it::class.java.name.endsWith(".KtNamedFunction")
            }.map { KtLsiMethod(it, true) }
        }
    
    private fun invokeMethod(methodName: String): Any? {
        return psiElement?.let { element ->
            try {
                val method = element::class.java.getMethod(methodName)
                method.invoke(element)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun invokeMethodOn(obj: Any, methodName: String): Any? {
        return try {
            val method = obj::class.java.getMethod(methodName)
            method.invoke(obj)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun checkIsPojoByReflection(): Boolean {
        val isInterface = invokeMethod("isInterface") as? Boolean ?: false
        val isEnum = invokeMethod("isEnum") as? Boolean ?: false
        val isData = invokeMethod("isData") as? Boolean ?: false
        
        @Suppress("UNCHECKED_CAST")
        val annotationEntries = invokeMethod("getAnnotationEntries") as? List<*> ?: emptyList<Any>()
        val annotationNames = annotationEntries.mapNotNull { entry ->
            entry?.let {
                try {
                    val shortName = invokeMethodOn(it, "getShortName")
                    shortName?.let { sn -> invokeMethodOn(sn, "asString") as? String }
                } catch (e: Exception) {
                    null
                }
            }
        }
        
        return site.addzero.util.lsi.assist.checkIsPojo(
            isInterface = isInterface,
            isEnum = isEnum,
            isAbstract = false,
            isDataClass = isData,
            annotationNames = annotationNames,
            isShortName = true  // 使用短名称匹配，因为 getShortName 返回的是简短名称
        )
    }
}
