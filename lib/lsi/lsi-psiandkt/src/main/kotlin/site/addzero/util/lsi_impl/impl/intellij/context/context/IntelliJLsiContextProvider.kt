package site.addzero.util.lsi_impl.impl.intellij.context.context

import com.intellij.openapi.project.Project
import site.addzero.util.lsi.context.LsiContext
import site.addzero.util.lsi_impl.impl.intellij.project.toVirtualFile
import site.addzero.util.lsi_impl.impl.intellij.virtualfile.toLsiClass

/**
 * IntelliJ平台的LSI上下文提供者
 *
 * 负责从IntelliJ Project中提取LSI上下文
 */
object IntelliJLsiContextProvider {

    /**
     * 从IntelliJ Project获取LSI上下文
     *
     * 策略：
     * 1. 获取当前选中的虚拟文件
     * 2. 使用toLsiClass()提取主类
     *
     * 注意：此方法使用基础的toLsiClass()，具体实现需要在应用模块中
     * 通过依赖lsi-psi和lsi-kt模块来提供完整实现
     */
    fun getLsiContext(project: Project): LsiContext {
        val virtualFile = project.toVirtualFile()
            ?: return LsiContext.EMPTY

        // 使用基础的toLsiClass方法 - 具体实现由应用模块提供
        val primaryClass = try {
            virtualFile.toLsiClass()
        } catch (e: NotImplementedError) {
            e.printStackTrace()
            null
        }

        // 为了保持轻量，暂时不实现 LsiFile和allClasses
        return LsiContext(
            currentClass = primaryClass,
            currentFile = null,
            filePath = virtualFile.path,
            allClassesInFile = primaryClass?.let { listOf(it) } ?: emptyList()
        )
    }
}

/**
 * Project扩展函数：获取LSI上下文
 *
 * 使用示例：
 * ```kotlin
 * val context = project.lsiContext()
 * val currentClass = context.currentClass
 * ```
 */
@Suppress("unused")
fun Project.lsiContext(): LsiContext {
    return IntelliJLsiContextProvider.getLsiContext(this)
}
