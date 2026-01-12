package site.addzero.util.lsi_impl.impl.intellij.element

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement

fun PsiElement?.getCurrentPsiElement(editor: Editor?): PsiElement? {
    val offset = editor?.caretModel?.offset ?: return null
    val element = this?.findElementAt(offset)
    return element
}
