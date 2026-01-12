package site.addzero.util.lsi_impl.impl.psi.virtualfile

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi_impl.impl.psi.clazz.PsiLsiClass

/**
 * PSI实现：从VirtualFile提取LSI类
 */

/**
 * 从Java文件中提取所有类并转换为LSI类
 */
@Suppress("unused")
fun VirtualFile.toAllLsiClasses(project: Project): List<LsiClass> {
    val psiFile = this.toPsiFile(project) ?: return emptyList()

    return when (psiFile) {
        is PsiJavaFile -> {
            psiFile.classes
                .map { PsiLsiClass(it) }
        }
        else -> emptyList()
    }
}

/**
 * 从Java文件中提取所有PsiClass
 */
@Suppress("unused")
fun VirtualFile.toAllPsiClasses(project: Project): List<PsiClass> {
    val psiFile = this.toPsiFile(project) as? PsiJavaFile
        ?: return emptyList()
    return psiFile.classes.toList()
}
