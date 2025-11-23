package site.addzero.gradle.plugin.aptbuddy

import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

abstract class GradleRefreshHelper @Inject constructor(
    private val providerFactory: ProviderFactory
) {

    fun requestGradleReevaluation(project: Project) {
        val markerFile = File(project.buildDir, ".apt-buddy-refresh-marker")
        markerFile.parentFile.mkdirs()
        markerFile.writeText(System.currentTimeMillis().toString())
        project.logger.lifecycle("Created refresh marker: ${markerFile.absolutePath}")

        project.plugins.forEach { plugin ->
            if (plugin.javaClass.simpleName.contains("Java")) {
                project.logger.info("Detected Java plugin, marking project for reevaluation")
            }
        }
    }

    fun needsForceRefresh(project: Project): Boolean {
        val markerFile = File(project.buildDir, ".apt-buddy-refresh-marker")
        return if (markerFile.exists()) {
            val lastRefresh = markerFile.readText().toLongOrNull() ?: 0
            val currentTime = System.currentTimeMillis()
            (currentTime - lastRefresh) > 30000
        } else {
            true
        }
    }

    fun generateIdeRefreshHint(project: Project, generatedFiles: List<File>) {
        val hintFile = File(project.buildDir, "apt-buddy-ide-hints.txt")
        hintFile.writeText(buildString {
            appendLine("APT Buddy IDE Refresh Hints")
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
