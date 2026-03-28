package site.addzero.kcp.i18n.plugin

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object I18NPluginKeys {
    const val compilerPluginId: String = "site.addzero.kcp.i18n"
    const val targetLocaleOption: String = "targetLocale"
    const val resourceBasePathOption: String = "resourceBasePath"
    const val generatedResourceFileOption: String = "generatedResourceFile"

    val targetLocaleKey: CompilerConfigurationKey<String> =
        CompilerConfigurationKey.create(targetLocaleOption)
    val resourceBasePathKey: CompilerConfigurationKey<String> =
        CompilerConfigurationKey.create(resourceBasePathOption)
    val generatedResourceFileKey: CompilerConfigurationKey<String> =
        CompilerConfigurationKey.create(generatedResourceFileOption)

    val runtimeFunctionCallableId: CallableId = CallableId(
        FqName("site.addzero.util"),
        Name.identifier("i18nT"),
    )
}
