package site.addzero.kcp.transformoverload.idea

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.facet.KotlinFacet

class TransformOverloadProjectActivity : ProjectActivity {

    private val logger = Logger.getInstance(TransformOverloadProjectActivity::class.java)

    override suspend fun execute(project: Project) {
        enableCommunityCompilerPlugins()
        logDetectedPluginClasspaths(project)
        restartAnalysis(project)
    }

    private fun enableCommunityCompilerPlugins() {
        val application = ApplicationManager.getApplication()
        val registryValue = Registry.get("kotlin.k2.only.bundled.compiler.plugins.enabled")
        if (!registryValue.asBoolean()) {
            logger.info("Kotlin K2 community compiler plugins are already enabled")
            return
        }
        registryValue.setValue(false, application)
        logger.info("Enabled Kotlin K2 community compiler plugins for transform-overload IDE support")
    }

    private fun logDetectedPluginClasspaths(project: Project) {
        val detected = ModuleManager.getInstance(project)
            .modules
            .mapNotNull { module ->
                val classpaths = KotlinFacet.get(module)
                    ?.configuration
                    ?.settings
                    ?.mergedCompilerArguments
                    ?.pluginClasspaths
                    ?.filter { classpath ->
                        "transform-overload" in classpath || "kcp-transform-overload-plugin" in classpath
                    }
                    .orEmpty()
                if (classpaths.isEmpty()) {
                    null
                } else {
                    "${module.name}: ${classpaths.joinToString()}"
                }
            }

        if (detected.isEmpty()) {
            logger.warn(
                "Transform-overload compiler plugin classpath was not found in Kotlin facet arguments. " +
                    "Gradle sync may be stale, so IDE support can still be unavailable.",
            )
        } else {
            detected.forEach { line ->
                logger.info("Detected transform-overload compiler plugin classpath for $line")
            }
        }
    }

    private fun restartAnalysis(project: Project) {
        val application = ApplicationManager.getApplication()
        application.invokeLater {
            if (project.isDisposed) {
                return@invokeLater
            }
            PsiManager.getInstance(project).dropPsiCaches()
            DaemonCodeAnalyzer.getInstance(project).restart()
        }
    }
}
