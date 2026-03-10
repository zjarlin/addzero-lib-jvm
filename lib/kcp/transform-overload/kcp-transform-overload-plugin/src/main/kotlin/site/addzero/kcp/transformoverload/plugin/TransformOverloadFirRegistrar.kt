package site.addzero.kcp.transformoverload.plugin

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class TransformOverloadFirRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::TransformOverloadFirExtension
    }
}
