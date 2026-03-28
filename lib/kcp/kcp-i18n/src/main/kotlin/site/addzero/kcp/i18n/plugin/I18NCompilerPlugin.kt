package site.addzero.kcp.i18n.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class I18NCompilerPlugin : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true
    override val pluginId: String = I18NPluginKeys.compilerPluginId

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val resourceBasePath = I18NPluginConfiguration.getResourceBasePath(configuration)
        val generatedCatalogFile = I18NPluginConfiguration.getGeneratedCatalogFile(configuration)
        IrGenerationExtension.registerExtension(
            I18NIrGenerationExtension(
                resourceBasePath = resourceBasePath,
                generatedCatalogFile = generatedCatalogFile,
            ),
        )
    }
}
