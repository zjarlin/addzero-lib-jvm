package site.addzero.kcp.transformoverload.plugin

import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

internal enum class ConverterParameterKind {
    EXTENSION_RECEIVER,
    VALUE_PARAMETER,
}

internal enum class LiftKind {
    NONE,
    ITERABLE,
    COLLECTION,
    LIST,
    SET,
    SEQUENCE,
}

internal data class FirConverterSpec(
    val symbol: FirNamedFunctionSymbol,
    val parameterKind: ConverterParameterKind,
    val sourceType: ConeKotlinType,
    val targetType: ConeKotlinType,
    val typeParameters: List<FirTypeParameterSymbol>,
    val uniqueSuffix: String,
    val callableIdText: String,
)

internal data class FirParameterTransform(
    val converter: FirConverterSpec,
    val parameterIndex: Int,
    val generatedParameterType: ConeKotlinType,
    val liftKind: LiftKind,
)

internal data class FirOverloadCandidate(
    val original: FirNamedFunctionSymbol,
    val generatedName: Name,
    val parameterTransforms: List<FirParameterTransform>,
)

internal data class IrConverterSpec(
    val function: IrSimpleFunction,
    val parameterKind: ConverterParameterKind,
    val sourceType: IrType,
    val targetType: IrType,
    val typeParameters: List<IrTypeParameter>,
    val uniqueSuffix: String,
    val callableIdText: String,
    val supportsMemberContainer: Boolean,
)

internal data class IrParameterTransform(
    val converter: IrConverterSpec,
    val parameterIndex: Int,
    val liftKind: LiftKind,
)

internal data class IrOverloadMatch(
    val original: IrSimpleFunction,
    val parameterTransforms: List<IrParameterTransform>,
)
