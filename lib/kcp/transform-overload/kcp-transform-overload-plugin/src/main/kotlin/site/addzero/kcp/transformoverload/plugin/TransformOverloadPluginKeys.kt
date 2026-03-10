package site.addzero.kcp.transformoverload.plugin

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.FqName

internal object TransformOverloadPluginKeys {
    const val compilerPluginId: String = "site.addzero.kcp.transform-overload"

    val generateTransformOverloadsAnnotation = FqName(
        "site.addzero.kcp.transformoverload.annotations.GenerateTransformOverloads",
    )
    val overloadTransformAnnotation = FqName(
        "site.addzero.kcp.transformoverload.annotations.OverloadTransform",
    )
    val transformProvider = FqName(
        "site.addzero.kcp.transformoverload.annotations.TransformProvider",
    )

    val iterableLiftFunction = FqName(
        "site.addzero.kcp.transformoverload.annotations.transformOverloadLiftIterable",
    )
    val collectionLiftFunction = FqName(
        "site.addzero.kcp.transformoverload.annotations.transformOverloadLiftCollection",
    )
    val listLiftFunction = FqName(
        "site.addzero.kcp.transformoverload.annotations.transformOverloadLiftList",
    )
    val setLiftFunction = FqName(
        "site.addzero.kcp.transformoverload.annotations.transformOverloadLiftSet",
    )
    val sequenceLiftFunction = FqName(
        "site.addzero.kcp.transformoverload.annotations.transformOverloadLiftSequence",
    )
}

internal object TransformOverloadGeneratedDeclarationKey : GeneratedDeclarationKey() {
    override fun toString(): String = "TransformOverloadGeneratedDeclarationKey"
}
