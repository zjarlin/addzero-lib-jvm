package site.addzero.ksp.singletonadapter.kcp

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class SingletonAdapterCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        // 总是注册扩展，但只在有注解时处理
        IrGenerationExtension.registerExtension(SingletonAdapterIrGenerationExtension())
    }
}
