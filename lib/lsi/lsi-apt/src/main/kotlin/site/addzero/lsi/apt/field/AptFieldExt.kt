package site.addzero.lsi.apt.field

import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.apt.clazz.AptLsiClass
import site.addzero.lsi.apt.field.toLsiField
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements


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
