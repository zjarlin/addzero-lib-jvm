package site.addzero.util.lsi_impl.impl.apt.element

import site.addzero.util.lsi.element.LsiElement
import javax.lang.model.element.Element

fun Element.toLsiElement(): LsiElement {
    val aptLsiElement = AptLsiElement(this)
    return aptLsiElement
}

class AptLsiElement(val element: Element) : LsiElement {
    override val isField: Boolean
        get() = element.isField()
    override val isClass: Boolean
        get() = element.isClass()
}
