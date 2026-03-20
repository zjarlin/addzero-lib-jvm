package site.addzero.kcp.allobjectjvmstatic.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class AllObjectJvmStaticCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = true
    override val pluginId: String = AllObjectJvmStaticPluginKeys.compilerPluginId

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(AllObjectJvmStaticIrGenerationExtension())
    }
}
