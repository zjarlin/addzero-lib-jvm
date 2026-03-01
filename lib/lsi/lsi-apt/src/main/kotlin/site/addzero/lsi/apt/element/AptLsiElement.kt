package site.addzero.lsi.apt.element

import site.addzero.lsi.apt.element.isClass
import site.addzero.lsi.apt.element.isField
import site.addzero.lsi.element.LsiElement
import javax.lang.model.element.Element

fun Element.toLsiElement(): LsiElement {
    val aptLsiElement = _root_ide_package_.site.addzero.lsi.apt.element.AptLsiElement(this)
    return aptLsiElement
}

class AptLsiElement(val element: Element) : LsiElement {
    override val isField: Boolean
        get() = element.isField()
    override val isClass: Boolean
        get() = element.isClass()
}
