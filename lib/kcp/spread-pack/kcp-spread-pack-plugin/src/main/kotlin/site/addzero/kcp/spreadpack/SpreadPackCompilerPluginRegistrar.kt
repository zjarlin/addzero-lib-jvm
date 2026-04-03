package site.addzero.kcp.transformoverload.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
class TransformOverloadCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = true
    override val pluginId: String = TransformOverloadPluginKeys.compilerPluginId

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(TransformOverloadFirRegistrar())
        IrGenerationExtension.registerExtension(TransformOverloadIrGenerationExtension())
    }
}
