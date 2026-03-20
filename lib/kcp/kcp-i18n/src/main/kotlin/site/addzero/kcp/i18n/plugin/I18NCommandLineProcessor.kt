package site.addzero.kcp.i18n.plugin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class I18NCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = I18NPluginKeys.compilerPluginId

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = I18NPluginKeys.targetLocaleOption,
            valueDescription = "<locale>",
            description = "Locale code used by generated i18n lookups.",
            required = false,
            allowMultipleOccurrences = false,
        ),
        CliOption(
            optionName = I18NPluginKeys.resourceBasePathOption,
            valueDescription = "<resource-base-path>",
            description = "Classpath-relative base path for i18n properties files.",
            required = false,
            allowMultipleOccurrences = false,
        ),
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        when (option.optionName) {
            I18NPluginKeys.targetLocaleOption -> {
                configuration.put(I18NPluginKeys.targetLocaleKey, value)
            }

            I18NPluginKeys.resourceBasePathOption -> {
                configuration.put(I18NPluginKeys.resourceBasePathKey, value)
            }

            else -> {
                error("Unknown option: ${option.optionName}")
            }
        }
    }
}
