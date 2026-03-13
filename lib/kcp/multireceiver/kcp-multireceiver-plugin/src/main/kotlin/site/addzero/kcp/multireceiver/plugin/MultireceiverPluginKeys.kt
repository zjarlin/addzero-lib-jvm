package site.addzero.kcp.multireceiver.plugin

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal object MultireceiverPluginKeys {
    const val compilerPluginId: String = "site.addzero.kcp.multireceiver"

    val addGenerateExtensionAnnotation = FqName(
        "site.addzero.kcp.annotations.AddGenerateExtension",
    )
    val receiverAnnotation = FqName(
        "site.addzero.kcp.annotations.Receiver",
    )
    val jvmNameAnnotation = FqName(
        "kotlin.jvm.JvmName",
    )
    val jvmNameAnnotationClassId: ClassId = ClassId.topLevel(jvmNameAnnotation)

    const val stubErrorMessage: String = "Multireceiver stub body should be lowered in IR"
}

internal object MultireceiverGeneratedDeclarationKey : GeneratedDeclarationKey() {
    override fun toString(): String = "MultireceiverGeneratedDeclarationKey"
}
