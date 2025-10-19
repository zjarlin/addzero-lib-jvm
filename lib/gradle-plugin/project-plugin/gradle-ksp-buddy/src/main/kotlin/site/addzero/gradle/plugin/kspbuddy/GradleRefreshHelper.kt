package site.addzero.gradle.plugin.kspbuddy

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

/**
 * Gradle 刷新助手类
 * 用于处理 Gradle 刷新和 IDE 同步问题
 */
abstract class GradleRefreshHelper @Inject constructor(
    private val providerFactory: ProviderFactory
) {

    /**
     * 触发 Gradle 重新评估项目结构
     * 这有助于 IDE 识别新生成的文件
     */
    fun requestGradleReevaluation(project: Project) {
        // 通过创建一个标记文件来触发 Gradle 重新评估
        val markerFile = File(project.buildDir, ".ksp-buddy-refresh-marker")
        markerFile.parentFile.mkdirs()
        markerFile.writeText(System.currentTimeMillis().toString())

        project.logger.lifecycle("Created refresh marker: ${markerFile.absolutePath}")

        // 通知 Gradle 输入发生变化
        project.plugins.forEach { plugin ->
            if (plugin.javaClass.simpleName.contains("Kotlin")) {
                project.logger.info("Detected Kotlin plugin, marking project for reevaluation")
            }
        }
    }

    /**
     * 检查是否需要强制刷新
     */
    fun needsForceRefresh(project: Project): Boolean {
        val markerFile = File(project.buildDir, ".ksp-buddy-refresh-marker")
        return if (markerFile.exists()) {
            val lastRefresh = markerFile.readText().toLongOrNull() ?: 0
            val currentTime = System.currentTimeMillis()
            // 如果超过 30 秒，可能需要强制刷新
            (currentTime - lastRefresh) > 30000
        } else {
            true
        }
    }

    /**
     * 为 IDE 生成提示信息
     */
    fun generateIdeRefreshHint(project: Project, generatedFiles: List<File>) {
        val hintFile = File(project.buildDir, "ksp-buddy-ide-hints.txt")
        hintFile.writeText(buildString {
            appendLine("KSP Buddy IDE Refresh Hints")
            appendLine("============================")
            appendLine("Generated files that may need IDE refresh:")
            generatedFiles.forEach { file ->
                appendLine("- ${file.absolutePath}")
            }
            appendLine()
            appendLine("If you see compilation errors, try:")
            appendLine("1. Gradle -> Refresh Gradle Project (IntelliJ)")
            appendLine("2. File -> Reload Gradle Projects")
            appendLine("3. Restart IDE if necessary")
            appendLine()
            appendLine("Generated at: ${System.currentTimeMillis()}")
        })

        project.logger.lifecycle("Generated IDE refresh hints: ${hintFile.absolutePath}")
    }
}