package site.addzero.kcp.i18n.plugin

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object I18NPluginKeys {
    const val compilerPluginId: String = "site.addzero.kcp.i18n"
    const val resourceBasePathOption: String = "resourceBasePath"
    const val generatedCatalogFileOption: String = "generatedCatalogFile"
    const val scanScopeOption: String = "scanScope"
    const val scanScopeAll: String = "all"
    const val scanScopeComposableOnly: String = "composableOnly"

    val resourceBasePathKey: CompilerConfigurationKey<String> =
        CompilerConfigurationKey.create(resourceBasePathOption)
    val generatedCatalogFileKey: CompilerConfigurationKey<String> =
        CompilerConfigurationKey.create(generatedCatalogFileOption)
    val scanScopeKey: CompilerConfigurationKey<String> =
        CompilerConfigurationKey.create(scanScopeOption)

    val runtimeFunctionCallableId: CallableId = CallableId(
        FqName("site.addzero.util"),
        Name.identifier("i18nT"),
    )

    val composableAnnotationFqName: FqName = FqName("androidx.compose.runtime.Composable")
}
