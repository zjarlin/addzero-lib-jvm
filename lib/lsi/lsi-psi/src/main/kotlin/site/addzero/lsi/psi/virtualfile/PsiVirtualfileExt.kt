package site.addzero.lsi.psi.virtualfile

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.intellij.virtualfile.guessProject
import site.addzero.lsi.psi.clazz.PsiLsiClass
import site.addzero.lsi.psi.psifile.convertToByChildren

fun VirtualFile.toPsiClass(project: Project): PsiClass? {
    val toPsiFile = this.toPsiFile(project)
    val ktClass = toPsiFile?.convertToByChildren<PsiClass>()
    return ktClass
}

/**
 * Convert Java VirtualFile to LsiClass
 * Converts a Java file to LsiClass representation
 *
 * @return LsiClass instance or null if file doesn't contain a valid Java class
 */
fun VirtualFile?.toJavaLsiClass(): LsiClass? {
    if (this == null) return null
    val project = guessProject()
    val psiFile = toPsiFile(project) as? PsiJavaFile ?: return null
    val psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass::class.java) ?: return null
    return PsiLsiClass(psiClass)
}
