package site.addzero.kcp.multireceiver.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
class MultireceiverCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = true
    override val pluginId: String = MultireceiverPluginKeys.compilerPluginId

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(MultireceiverFirRegistrar())
        IrGenerationExtension.registerExtension(MultireceiverIrGenerationExtension())
    }
}
