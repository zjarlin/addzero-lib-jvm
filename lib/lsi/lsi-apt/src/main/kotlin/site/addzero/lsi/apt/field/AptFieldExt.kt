package site.addzero.lsi.apt.field

import site.addzero.lsi.field.LsiField
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.util.Elements

fun VariableElement.isEnum(): Boolean {
  // 核心判断逻辑
  val fieldType = asType()
  val isEnum = fieldType.kind == TypeKind.DECLARED
    && (fieldType as DeclaredType).asElement().let { element ->
    element is TypeElement && element.kind == ElementKind.ENUM
  }
  return isEnum
}

/**
 * 获取VariableElement的文档注释
 * 需要传入Elements实例来获取文档注释
 */
fun VariableElement.getDocComment(elements: Elements): String? {
  val enclosingElement1 = this.enclosingElement
  val docComment = elements.getDocComment(this)
  return docComment
}

/**
 * 批量转换VariableElement列表
 */
fun Collection<VariableElement>.toLsiFields(elements: Elements): List<LsiField> {
  val map = map {
    val toLsiField = it.toLsiField(elements)
    toLsiField
  }
  return map
}

fun VariableElement.toLsiField(elements: Elements): LsiField {
  val aptLsiField = _root_ide_package_.site.addzero.lsi.apt.field.AptLsiField(elements, this)
  return aptLsiField
}

//fun RoundEnvironment.toKldResolver(processingEnv: ProcessingEnvironment): Unit
