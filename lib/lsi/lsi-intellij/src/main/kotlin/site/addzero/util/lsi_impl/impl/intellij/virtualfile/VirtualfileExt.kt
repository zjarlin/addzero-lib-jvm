package site.addzero.util.lsi_impl.impl.intellij.virtualfile

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.vfs.VirtualFile
import site.addzero.util.lsi.constant.Language
import com.intellij.openapi.roots.ProjectFileIndex

/**
 * VirtualFile 扩展函数集合
 *
 * 提供便捷的诊断信息访问接口
 * 支持自动推导 Project 或显式传入 DiagnosticCacheAccessor
 */

/**
 * 尝试从 VirtualFile 推导出所属的 Project
 *
 * 查找逻辑：
 * 1. 遍历所有打开的项目
 * 2. 检查文件是否在项目的内容根目录下
 * 3. 返回第一个匹配的项目
 *
 * @return 文件所属的项目，如果找不到或文件在多个项目中则返回 null
 */
fun VirtualFile.inferProject(): Project? {
    val projectManager = com.intellij.openapi.project.ProjectManager.getInstance()
    val openProjects = projectManager.openProjects

    // 查找包含此文件的项目
    val matchingProjects = openProjects.filter { project ->
        val fileIndex = com.intellij.openapi.roots.ProjectFileIndex.getInstance(project)
        fileIndex.isInContent(this)
    }

    // 如果只有一个匹配的项目，返回它
    // 如果有多个或没有，返回 null（避免歧义）
    return matchingProjects.singleOrNull()
}

/**
 * 获取文件所属的项目，如果无法推导则抛出异常
 *
 * @throws IllegalStateException 如果无法推导出项目
 */
fun VirtualFile.requireProject(): Project {
    return inferProject() ?: throw IllegalStateException(
        "Cannot infer project for file: $path. " +
            "The file may not be in any open project, or it exists in multiple projects. " +
            "Please use the explicit project parameter: file.problems(accessor)"
    )
}

// ========== 诊断相关扩展函数（已移至 problem2prompt 模块） ==========

// 诊断功能已移至 DiagnosticExtensions.kt，避免跨模块依赖
// 如需使用诊断功能，请使用 site.addzero.diagnostic.service.DiagnosticExtensions 中的扩展函数

/**
 * 获取Kotlin类文件中的实体类定义
 *
 * @param project 可选参数，如果不提供则通过ProjectLocator自动推断
 */
fun VirtualFile.guessProject(): Project {
    val project = ProjectLocator.getInstance().guessProjectForFile(this)
    return project ?: throw IllegalStateException("Cannot determine project for file: ${this.path}")
}

/**
 * 扩展属性：获取 VirtualFile 的语言类型
 * 根据文件扩展名判断语言类型
 * @return Language 枚举值，包括 Java、Kotlin 等
 * @throws IllegalArgumentException 当文件类型不支持时抛出异常
 */
val VirtualFile.language: Language
    get() {
        return when (val fileType = extension) {
            "java" -> Language.Java
            "kt" -> Language.Kotlin
            else -> throw IllegalArgumentException("Unsupported file type: ${fileType ?: "<no-type>"}")
        }
    }