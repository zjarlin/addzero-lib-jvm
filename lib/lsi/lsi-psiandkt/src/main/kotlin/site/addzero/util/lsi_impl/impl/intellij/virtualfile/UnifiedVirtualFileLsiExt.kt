package site.addzero.util.lsi_impl.impl.intellij.virtualfile

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi_impl.impl.kt2.ext.toLsiClassesK2
import site.addzero.util.lsi_impl.impl.psi.clazz.PsiLsiClass

/**
 * 统一的VirtualFile到LSI转换实现
 *
 * 自动识别Java/Kotlin并转换为相应的LSI类
 * 此模块依赖于lsi-psi和lsi-kt2（K2 Analysis API）
 */

/**
 * 从VirtualFile提取所有类（支持Java和Kotlin）
 * Kotlin 使用 K2 Analysis API 解析
 *
 * 内部实现函数，公开 API 请使用 VirtualfileExt.kt 中的扩展函数
 */
 fun VirtualFile.toAllLsiClassesUnified(): List<LsiClass> {
    val project = this.guessProject()
    val psiFile = PsiManager.getInstance(project).findFile(this) ?: return emptyList()

    return when (psiFile) {
        is PsiJavaFile -> psiFile.classes.map { PsiLsiClass(it) }
        is KtFile -> psiFile.declarations
            .filterIsInstance<KtClass>()
            .takeIf { it.isNotEmpty() }
            ?.toLsiClassesK2()
            ?: emptyList()
        else -> emptyList()
    }
}

/**
 * 从VirtualFile提取主类（支持Java和Kotlin）
 *
 * 内部实现函数，公开 API 请使用 VirtualfileExt.kt 中的扩展函数
 */
internal fun VirtualFile.toPrimaryLsiClassUnified(): LsiClass? {
    val allClasses = toAllLsiClassesUnified()
    if (allClasses.isEmpty()) return null
    if (allClasses.size == 1) return allClasses.first()

    // 尝试找到与文件名匹配的类
    val fileNameWithoutExt = nameWithoutExtension
    return allClasses.firstOrNull { it.name == fileNameWithoutExt }
        ?: allClasses.first()
}
