package site.addzero.util.lsi_impl.impl.apt.element

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.util.Elements

/**
 * 获取元素的文档注释
 */
fun Element.getDocComment(elements: Elements): String? {
    return elements.getDocComment(this)
}

fun Element.isField(): Boolean {
    return this.kind == ElementKind.FIELD
}

fun Element.isClass(): Boolean {
    return this.kind == ElementKind.CLASS
}
