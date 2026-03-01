package site.addzero.lsi.apt.clazz

import site.addzero.lsi.clazz.LsiClass
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * 批量转换TypeElement列表
 */
fun Collection<TypeElement>.toLsiClasses(elements: Elements): List<LsiClass> {
    val map = map {
        val aptLsiClass = _root_ide_package_.site.addzero.lsi.apt.clazz.AptLsiClass(elements, it)
        aptLsiClass
    }
    return map
}

fun TypeElement.toLsiClass(elements: Elements): LsiClass =
    _root_ide_package_.site.addzero.lsi.apt.clazz.AptLsiClass(elements, this)


