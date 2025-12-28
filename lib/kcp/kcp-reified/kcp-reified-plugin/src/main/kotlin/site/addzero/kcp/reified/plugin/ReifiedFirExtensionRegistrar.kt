package site.addzero.kcp.reified.plugin

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

/**
 * FIR (K2) entrypoint for registering plugin extensions.
 */
class ReifiedFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::ReifiedFirDeclarationGenerationExtension
    }
}
