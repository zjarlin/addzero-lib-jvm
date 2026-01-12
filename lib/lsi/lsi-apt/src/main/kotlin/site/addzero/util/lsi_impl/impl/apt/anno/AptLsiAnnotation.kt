package site.addzero.util.lsi_impl.impl.apt.anno

import site.addzero.util.lsi.anno.LsiAnnotation
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue

class AptLsiAnnotation(private val annotationMirror: AnnotationMirror) : LsiAnnotation {

    override val qualifiedName: String? by lazy {
        annotationMirror.annotationType.toString()
    }

    override val simpleName: String? by lazy {
        annotationMirror.annotationType.asElement().simpleName.toString()
    }

    override val attributes: Map<String, Any?> by lazy {
        annotationMirror.elementValues.entries.associate { (key, value) ->
            key.simpleName.toString() to extractValue(value)
        }
    }

    override fun getAttribute(name: String): Any? = attributes[name]

    override fun hasAttribute(name: String): Boolean = attributes.containsKey(name)

    private fun extractValue(value: AnnotationValue): Any? {
        val v = value.value
        return when (v) {
            is List<*> -> v.firstNotNullOfOrNull { (it as? AnnotationValue)?.value?.toString() } ?: v.firstOrNull()?.toString()
            else -> v?.toString()?.removeSurrounding("\"")
        }
    }
}

fun AnnotationMirror.toLsiAnnotation(): LsiAnnotation = AptLsiAnnotation(this)

fun List<AnnotationMirror>.toLsiAnnotations(): List<LsiAnnotation> = map { it.toLsiAnnotation() }
