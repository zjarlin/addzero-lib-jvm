package site.addzero.util.lsi_impl.impl.ksp.method

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.method.LsiMethod
import site.addzero.util.lsi.method.LsiParameter
import site.addzero.util.lsi.type.LsiType
import site.addzero.util.lsi_impl.impl.ksp.anno.KspLsiAnnotation
import site.addzero.util.lsi_impl.impl.ksp.clazz.KspLsiClass
import site.addzero.util.lsi_impl.impl.ksp.type.KspLsiType

class KspLsiMethod(
    private val resolver: Resolver,
    private val ksFunctionDeclaration: KSFunctionDeclaration
) : LsiMethod {

    override val name: String? by lazy {
        ksFunctionDeclaration.simpleName.asString()
    }

    override val returnType: LsiType? by lazy {
        ksFunctionDeclaration.returnType?.let {
            KspLsiType(resolver, it.resolve())
        }
    }

    override val returnTypeName: String? by lazy {
        ksFunctionDeclaration.returnType?.resolve()?.declaration?.simpleName?.asString()
    }

    override val comment: String? by lazy {
        ksFunctionDeclaration.docString
    }

    override val annotations: List<LsiAnnotation> by lazy {
        ksFunctionDeclaration.annotations
            .map { KspLsiAnnotation(it) }
            .toList()
    }

    override val isStatic: Boolean by lazy {
        // Kotlin函数通常不是静态的，除非在companion object中
        val parent = ksFunctionDeclaration.parentDeclaration
        parent is KSClassDeclaration && parent.classKind == ClassKind.OBJECT
    }

    override val isAbstract: Boolean by lazy {
        ksFunctionDeclaration.modifiers.contains(Modifier.ABSTRACT)
    }

    override val parameters: List<LsiParameter> by lazy {
        ksFunctionDeclaration.parameters
            .map { KspLsiParameter(resolver, it) }
    }

    override val declaringClass: LsiClass? by lazy {
        val parent = ksFunctionDeclaration.parentDeclaration
        if (parent is KSClassDeclaration) {
            KspLsiClass(resolver, parent)
        } else null
    }
}

class KspLsiParameter(
    private val resolver: Resolver,
    private val ksValueParameter: KSValueParameter
) : LsiParameter {

    override val name: String? by lazy {
        ksValueParameter.name?.asString()
    }

    override val type: LsiType? by lazy {
        KspLsiType(resolver, ksValueParameter.type.resolve())
    }

    override val typeName: String? by lazy {
        ksValueParameter.type.resolve().declaration.simpleName.asString()
    }

    override val annotations: List<LsiAnnotation> by lazy {
        ksValueParameter.annotations
            .map { KspLsiAnnotation(it) }
            .toList()
    }

    override val hasDefault: Boolean by lazy {
        ksValueParameter.hasDefault
    }
}

//fun KSFunctionDeclaration.toLsiMethod(resolver: Resolver): LsiMethod = KspLsiMethod(resolver, this)

