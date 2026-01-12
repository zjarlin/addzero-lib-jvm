package site.addzero.util.lsi_impl.impl.psi.virtualfile

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import site.addzero.util.lsi.file.LsiFile
import site.addzero.util.lsi_impl.impl.intellij.virtualfile.guessProject
import site.addzero.util.lsi_impl.impl.psi.file.PsiLsiFile

/**
 * 将 VirtualFile 转换为 Java LsiFile
 *
 * @return LsiFile 实例，如果转换失败则返回 null
 */
fun VirtualFile?.toJavaLsiFile(): LsiFile? {
    this ?: return null
    val project = this.guessProject() ?: return null
    val psiFile = PsiManager.getInstance(project).findFile(this) as? PsiJavaFile ?: return null
    return PsiLsiFile(psiFile)
}
