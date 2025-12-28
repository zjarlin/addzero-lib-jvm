package site.addzero.kcp.reified.plugin

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

/**
 * Reified 方法生成编译器插件
 */
@OptIn(ExperimentalCompilerApi::class)
class ReifiedCompilerPlugin : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        println("[ReifiedPlugin] ===== Plugin registered! =====")
        FirExtensionRegistrarAdapter.registerExtension(ReifiedFirExtensionRegistrar())
    }
}
