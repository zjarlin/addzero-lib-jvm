package site.addzero.ide.init

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import site.addzero.ide.config.ui.AutoDDLConfigurable

class AutoDDLConfigRegistrar : ProjectActivity {
    override suspend fun execute(project: Project) {
        // AutoDDLConfigurable 会通过 plugin.xml 中的配置自动注册
        // 这里不需要额外的代码来启用 settings UI
    }
}