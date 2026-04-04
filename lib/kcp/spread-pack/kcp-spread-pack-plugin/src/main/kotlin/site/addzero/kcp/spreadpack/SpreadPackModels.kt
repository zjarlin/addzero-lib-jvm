package site.addzero.kcp.spreadpack

import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

internal enum class SelectorKind {
    PROPS,
    ATTRS,
    CALLBACKS,
}

internal data class FirSpreadPackField(
    val parameter: FirValueParameter,
    val resolvedType: ConeKotlinType,
)

internal data class FirSpreadPackExpansion(
    val parameterIndex: Int,
    val carrierClassId: ClassId,
    val selectorKind: SelectorKind,
    val excludedNames: Set<String>,
    val fields: List<FirSpreadPackField>,
)

internal data class FirSpreadPackCandidate(
    val original: FirNamedFunctionSymbol,
    val generatedName: Name,
    val expansions: List<FirSpreadPackExpansion>,
    val generatedParameterTypes: List<ConeKotlinType>,
)

internal data class IrSpreadPackField(
    val name: Name,
    val type: IrType,
    val constructorIndex: Int,
    val defaultValue: IrExpressionBody?,
)

internal data class IrSpreadPackExpansion(
    val parameterIndex: Int,
    val carrierClass: IrClass,
    val constructor: IrConstructor,
    val selectorKind: SelectorKind,
    val excludedNames: Set<String>,
    val fields: List<IrSpreadPackField>,
)

internal data class IrSpreadPackMatch(
    val original: IrSimpleFunction,
    val expansions: List<IrSpreadPackExpansion>,
)
