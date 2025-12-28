package site.addzero.kcp.plugin

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.CallableId

class AddGenerateExtensionFirExtension(session: FirSession) : FirDeclarationGenerationExtension(session) {

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        // Register predicates for function generation
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        // TODO: Implement FIR-based function generation
        // FIR API is complex and requires deep understanding of compiler internals
        return emptyList()
    }

    override fun getCallableNamesForClass(classSymbol: org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol<*>, context: MemberGenerationContext): Set<org.jetbrains.kotlin.name.Name> {
        return emptySet()
    }
}
