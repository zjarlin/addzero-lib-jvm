package site.addzero.util.lsi_impl.impl.ksp.anno

import com.google.devtools.ksp.symbol.KSAnnotation
import site.addzero.util.lsi.anno.LsiAnnotation

class KspLsiAnnotation(
    private val ksAnnotation: KSAnnotation
) : LsiAnnotation {

    override val qualifiedName: String? by lazy {
        ksAnnotation.annotationType.resolve().declaration.qualifiedName?.asString()
    }

    override val simpleName: String? by lazy {
        ksAnnotation.annotationType.resolve().declaration.simpleName.asString()
    }

    override val attributes: Map<String, Any?> by lazy {
        ksAnnotation.arguments.associate { argument ->
            (argument.name?.asString() ?: "") to argument.value
        }
    }

    override fun getAttribute(name: String): Any? {
        return attributes[name]
    }

    override fun hasAttribute(name: String): Boolean {
        return attributes.containsKey(name)
    }
}

fun KSAnnotation.toLsiAnnotation(): LsiAnnotation = KspLsiAnnotation(this)
