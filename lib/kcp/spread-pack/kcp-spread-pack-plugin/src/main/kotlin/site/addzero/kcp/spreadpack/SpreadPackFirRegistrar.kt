package site.addzero.kcp.spreadpack

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class SpreadPackFirRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::SpreadPackFirExtension
    }
}
