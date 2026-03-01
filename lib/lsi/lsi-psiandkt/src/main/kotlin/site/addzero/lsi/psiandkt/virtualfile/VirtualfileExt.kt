package site.addzero.lsi.psiandkt.virtualfile

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.vfs.VirtualFile
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.constant.Language
import site.addzero.lsi.file.LsiFile
import site.addzero.lsi.intellij.virtualfile.language
import site.addzero.lsi.psi.virtualfile.toJavaLsiClass
import site.addzero.lsi.psi.virtualfile.toJavaLsiFile

/**
 * 从 VirtualFile 推测所属的 Project
 *
 * 使用 ProjectLocator 查找包含此文件的项目
 * @throws IllegalStateException 如果无法确定项目
 */
fun VirtualFile.guessProject(): Project {
    return ProjectLocator.getInstance().guessProjectForFile(this)
        ?: error("无法从 VirtualFile 推测 Project: $path")
}


/**
 * 将 VirtualFile 转换为 LsiFile
 *
 * 根据文件类型自动选择合适的转换方法
 * 注意：Kotlin 文件暂不支持 LsiFile，请使用 toAllLsiClasses() 获取类列表
 *
 * @return LsiFile 实例，如果转换失败则返回 null
 */
fun VirtualFile?.toLsiFile(): LsiFile? {
    this ?: return null

    return when (language) {
        Language.Java -> toJavaLsiFile()
        Language.Kotlin -> null  // K2 Analysis API 不支持 LsiFile 抽象
    }
}

/**
 * 将 VirtualFile 转换为 LsiClass（主类）
 *
 * 支持 Java 和 Kotlin 文件
 */
fun VirtualFile?.toLsiClass(): LsiClass? {
    this ?: return null

    return when (language) {
        Language.Java -> toJavaLsiClass()
        Language.Kotlin -> toPrimaryLsiClassUnified()
    }
}

/**
 * 获取文件中的所有类
 *
 * 支持 Java 和 Kotlin 文件
 */
fun VirtualFile?.toAllLsiClasses(): List<LsiClass> {
    this ?: return emptyList()
    return toAllLsiClassesUnified()
}
