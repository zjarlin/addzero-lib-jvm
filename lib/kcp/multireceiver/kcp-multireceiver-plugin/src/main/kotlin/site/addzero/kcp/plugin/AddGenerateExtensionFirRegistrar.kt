package site.addzero.kcp.plugin

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class AddGenerateExtensionFirRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::AddGenerateExtensionFirExtension
    }
}
