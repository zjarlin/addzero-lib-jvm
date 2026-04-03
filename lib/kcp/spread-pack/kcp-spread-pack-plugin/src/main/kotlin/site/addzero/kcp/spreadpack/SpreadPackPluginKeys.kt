package site.addzero.kcp.spreadpack

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object SpreadPackPluginKeys {
    const val compilerPluginId: String = "site.addzero.kcp.spread-pack"
    const val stubErrorMessage: String = "Spread pack stub body should be lowered in IR"

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
    val spreadArgsOfAnnotation = FqName(
        "site.addzero.kcp.spreadpack.SpreadArgsOf",
    )
    val spreadArgsOfAnnotationClassId = ClassId.topLevel(
        spreadArgsOfAnnotation,
    )
    val spreadOverloadAnnotation = FqName(
        "site.addzero.kcp.spreadpack.SpreadOverload",
    )
    val spreadOverloadAnnotationClassId = ClassId.topLevel(
        spreadOverloadAnnotation,
    )
    val spreadOverloadsOfAnnotation = FqName(
        "site.addzero.kcp.spreadpack.SpreadOverloadsOf",
    )
    val spreadOverloadsOfAnnotationClassId = ClassId.topLevel(
        spreadOverloadsOfAnnotation,
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
