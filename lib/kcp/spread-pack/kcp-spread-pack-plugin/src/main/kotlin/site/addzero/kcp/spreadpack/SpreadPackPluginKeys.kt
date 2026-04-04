package site.addzero.kcp.spreadpack

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object SpreadPackPluginKeys {
    const val compilerPluginId = "site.addzero.kcp.spread-pack"
    const val stubErrorMessage = "Spread pack stub body should be lowered in IR"

    val generateSpreadPackOverloadsAnnotation = FqName(
        "site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads",
    )
    val generateSpreadPackOverloadsAnnotationClassId = ClassId.topLevel(
        generateSpreadPackOverloadsAnnotation,
    )
    val spreadPackAnnotation = FqName(
        "site.addzero.kcp.spreadpack.SpreadPack",
    )
    val spreadPackAnnotationClassId = ClassId.topLevel(
        spreadPackAnnotation,
    )
    val spreadPackOfAnnotation = FqName(
        "site.addzero.kcp.spreadpack.SpreadPackOf",
    )
    val spreadPackOfAnnotationClassId = ClassId.topLevel(
        spreadPackOfAnnotation,
    )
    val spreadPackCarrierOfAnnotation = FqName(
        "site.addzero.kcp.spreadpack.SpreadPackCarrierOf",
    )
    val spreadPackCarrierOfAnnotationClassId = ClassId.topLevel(
        spreadPackCarrierOfAnnotation,
    )
    val spreadArgsOfAnnotation = FqName(
        "site.addzero.kcp.spreadpack.SpreadArgsOf",
    )
    val spreadArgsOfAnnotationClassId = ClassId.topLevel(
        spreadArgsOfAnnotation,
    )
    val generatedSpreadPackOverloadAnnotation = FqName(
        "site.addzero.kcp.spreadpack.GeneratedSpreadPackOverload",
    )
    val generatedSpreadPackOverloadAnnotationClassId = ClassId.topLevel(
        generatedSpreadPackOverloadAnnotation,
    )
}

internal object SpreadPackGeneratedDeclarationKey : GeneratedDeclarationKey() {
    override fun toString(): String = "SpreadPackGeneratedDeclarationKey"
}
