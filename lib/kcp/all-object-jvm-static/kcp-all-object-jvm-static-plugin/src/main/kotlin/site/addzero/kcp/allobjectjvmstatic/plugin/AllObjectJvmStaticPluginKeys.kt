package site.addzero.kcp.allobjectjvmstatic.plugin

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.FqName

internal object AllObjectJvmStaticPluginKeys {
    const val compilerPluginId: String = "site.addzero.kcp.all-object-jvm-static"

    val jvmStaticAnnotation = FqName("kotlin.jvm.JvmStatic")
}

internal object AllObjectJvmStaticGeneratedDeclarationKey : GeneratedDeclarationKey() {
    override fun toString(): String = "AllObjectJvmStaticGeneratedDeclarationKey"
}
