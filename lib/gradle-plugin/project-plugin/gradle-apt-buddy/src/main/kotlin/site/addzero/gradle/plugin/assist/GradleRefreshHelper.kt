package site.addzero.gradle.plugin.assist

import org.gradle.api.Project
import java.io.File
import javax.inject.Inject

abstract class GradleRefreshHelper @Inject constructor() {

    fun requestGradleReevaluation(project: Project) {
        val markerFile = File(project.layout.buildDirectory.get().asFile, ".apt-buddy-refresh-marker")
        markerFile.parentFile.mkdirs()
        markerFile.writeText(System.currentTimeMillis().toString())
        project.logger.lifecycle("Created refresh marker: ${markerFile.absolutePath}")

        project.plugins.forEach { plugin ->
            if (plugin.javaClass.simpleName.contains("Java")) {
                project.logger.info("Detected Java plugin, marking project for reevaluation")
            }
        }
    }

    fun generateIdeRefreshHint(project: Project, generatedFiles: List<File>) {
        val hintFile = File(project.layout.buildDirectory.get().asFile, "apt-buddy-ide-hints.txt")
        hintFile.writeText("""
            |APT Buddy IDE Refresh Hints
            |============================
            |Generated files that may need IDE refresh:
            |${generatedFiles.joinToString("\n") { "- ${it.absolutePath}" }}
            |
            |If you see compilation errors, try:
            |1. Gradle -> Refresh Gradle Project (IntelliJ)
            |2. File -> Reload Gradle Projects
            |3. Restart IDE if necessary
            |
            |Generated at: ${System.currentTimeMillis()}
        """.trimMargin())

        project.logger.lifecycle("Generated IDE refresh hints: ${hintFile.absolutePath}")
    }
}
