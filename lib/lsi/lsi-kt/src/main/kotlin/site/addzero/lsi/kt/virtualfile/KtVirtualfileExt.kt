package site.addzero.lsi.kt.virtualfile

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.intellij.virtualfile.guessProject
import site.addzero.lsi.kt.clazz.KtLsiClass

/**
 * Convert Kotlin VirtualFile to LsiClass
 * Converts a Kotlin file to LsiClass representation
 *
 * @return LsiClass instance or null if file doesn't contain a valid Kotlin class
 */
fun VirtualFile?.toKotlinLsiClass(): LsiClass? {
    if (this == null) return null
    val project = guessProject() ?: return null
    val psiFile = toPsiFile(project) as? KtFile ?: return null
    val ktClass = PsiTreeUtil.findChildOfType(psiFile, KtClass::class.java) ?: return null
    return KtLsiClass(ktClass)
}
