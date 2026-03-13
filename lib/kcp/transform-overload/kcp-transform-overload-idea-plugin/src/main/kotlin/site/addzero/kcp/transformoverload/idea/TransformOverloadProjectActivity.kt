package site.addzero.kcp.transformoverload.idea

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.PsiManager

class TransformOverloadProjectActivity : ProjectActivity {

    private val logger = Logger.getInstance(TransformOverloadProjectActivity::class.java)

    override suspend fun execute(project: Project) {
        logger.info("Scheduling transform-overload IDEA stub refresh")
        project.getService(TransformOverloadStubService::class.java).scheduleRefresh()
        restartAnalysis(project)
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
