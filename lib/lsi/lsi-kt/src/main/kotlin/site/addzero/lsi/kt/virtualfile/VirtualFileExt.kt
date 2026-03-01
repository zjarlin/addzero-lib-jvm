package site.addzero.lsi.kt.virtualfile

import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile
import site.addzero.lsi.file.LsiFile
import site.addzero.lsi.kt.file.KtLsiFile

/**
 * 将 VirtualFile 转换为 Kotlin LsiFile
 * 
 * @return LsiFile 实例，如果转换失败则返回 null
 */
fun VirtualFile?.toKotlinLsiFile(): LsiFile? {
    this ?: return null
    val project = ProjectLocator.getInstance().guessProjectForFile(this) ?: return null
    val psiFile = PsiManager.getInstance(project).findFile(this) as? KtFile ?: return null
    return KtLsiFile(psiFile)
}
