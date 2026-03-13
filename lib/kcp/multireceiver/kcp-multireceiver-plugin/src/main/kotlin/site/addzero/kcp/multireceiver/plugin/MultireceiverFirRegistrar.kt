package site.addzero.kcp.multireceiver.plugin

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class MultireceiverFirRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::MultireceiverFirExtension
    }
}
