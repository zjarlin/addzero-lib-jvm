package site.addzero.kcp.multireceiver.plugin

import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

internal enum class GenerationKind {
    EXTENSION,
    CONTEXT,
}

internal data class FirWrapperCandidate(
    val original: FirNamedFunctionSymbol,
    val generationKind: GenerationKind,
    val receiverParameterIndex: Int?,
    val contextParameterIndices: List<Int>,
    val generatedJvmName: String,
)

internal data class IrWrapperMatch(
    val original: IrSimpleFunction,
    val generationKind: GenerationKind,
    val receiverParameterIndex: Int?,
    val contextParameterIndices: List<Int>,
)
