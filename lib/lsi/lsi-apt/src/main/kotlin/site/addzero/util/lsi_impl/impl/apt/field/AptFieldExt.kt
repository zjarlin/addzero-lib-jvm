package site.addzero.util.lsi_impl.impl.apt.field

import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi_impl.impl.apt.clazz.AptLsiClass
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
    val aptLsiField = AptLsiField(elements, this)
    return aptLsiField
}


//fun RoundEnvironment.toKldResolver(processingEnv: ProcessingEnvironment): Unit
