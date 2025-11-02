package site.addzero.ide.init

import com.intellij.codeInsight.intention.IntentionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import site.addzero.ide.config.ui.AutoDDLConfigurable

class AutoDDLConfigRegistrar : ProjectActivity {
    override suspend fun execute(project: Project) {
        registerIntentions()
//        AutoDDLConfigurableProvider()

                AutoDDLConfigurable()

    }
}

