package site.addzero.kcp.spreadpack

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
class SpreadPackCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2 = true
    override val pluginId = SpreadPackPluginKeys.compilerPluginId

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(SpreadPackFirRegistrar())
        IrGenerationExtension.registerExtension(SpreadPackIrGenerationExtension())
    }
}
