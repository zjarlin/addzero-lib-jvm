package site.addzero.kcp.i18n.plugin

import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * 国际化插件配置
 */
class I18NPluginConfiguration {
    companion object {
        // 从编译器配置中获取资源基础路径
        fun getResourceBasePath(configuration: CompilerConfiguration): String {
            return configuration.get(I18NPluginKeys.resourceBasePathKey) ?: "i18n"
        }

        fun getGeneratedCatalogFile(configuration: CompilerConfiguration): String? {
            return configuration.get(I18NPluginKeys.generatedCatalogFileKey)
        }
    }
}
