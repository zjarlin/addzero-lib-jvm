package site.addzero.lsi.apt.anno

import site.addzero.lsi.assist.*
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue

fun AnnotationMirror.getAttribute(attrName: String): String? {
  return elementValues.entries.find { it.key.simpleName.toString() == attrName }
    ?.value?.let { it.extractStringValue() }
}

fun AnnotationMirror.getArgFirst(): String? {
  return elementValues.values.firstOrNull()?.let { it.extractStringValue() }
}

fun AnnotationValue.extractStringValue(): String? {
  val v = this.value
  return when (v) {
    is String -> v
    is List<*> -> v.firstNotNullOfOrNull { (it as? AnnotationValue)?.value?.toString() }?.removeSurrounding("\"")
    else -> v?.toString()?.removeSurrounding("\"")
  }
}

fun List<AnnotationMirror>.fieldComment(): String? {
  val guessFieldCommentOrNull = this.iterator().guessFieldCommentOrNull(
    getQualifiedName = { it.annotationType.toString() },
    getAttributeValue = { anno, attrName -> anno.getAttribute(attrName) }
  )
  return guessFieldCommentOrNull
}

fun List<AnnotationMirror>.methodComment(): String? {
  return this.iterator().guessMethodCommentOrNull(
    getQualifiedName = { it.annotationType.toString() },
    getAttributeValue = { anno, attrName -> anno.getAttribute(attrName) }
  )
}

fun List<AnnotationMirror>.classComment(): String? {
  return this.iterator().guessClassCommentOrNull(
    getQualifiedName = { it.annotationType.toString() },
    getAttributeValue = { anno, attrName -> anno.getAttribute(attrName) }
  )
}
