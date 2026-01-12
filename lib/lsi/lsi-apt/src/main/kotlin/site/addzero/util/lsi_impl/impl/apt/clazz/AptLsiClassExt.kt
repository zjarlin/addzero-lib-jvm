package site.addzero.util.lsi_impl.impl.apt.clazz

import site.addzero.util.lsi.clazz.LsiClass
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * 批量转换TypeElement列表
 */
fun Collection<TypeElement>.toLsiClasses(elements: Elements): List<LsiClass> {
    val map = map {
        val aptLsiClass = AptLsiClass(elements, it)
        aptLsiClass
    }
    return map
}

fun TypeElement.toLsiClass(elements: Elements): LsiClass = AptLsiClass(elements, this)


