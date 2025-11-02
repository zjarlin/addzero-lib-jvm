package site.addzero.ide.init

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import site.addzero.ide.config.ui.AutoDDLConfigurable

class AutoDDLConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable {
        return AutoDDLConfigurable()
    }
}
