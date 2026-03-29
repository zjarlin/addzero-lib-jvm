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
            optionName = I18NPluginKeys.resourceBasePathOption,
            valueDescription = "<resource-base-path>",
            description = "Classpath-relative base path for i18n properties files.",
            required = false,
            allowMultipleOccurrences = false,
        ),
        CliOption(
            optionName = I18NPluginKeys.generatedCatalogFileOption,
            valueDescription = "<generated-catalog-file>",
            description = "Absolute path of the generated source-language catalog file.",
            required = false,
            allowMultipleOccurrences = false,
        ),
        CliOption(
            optionName = I18NPluginKeys.scanScopeOption,
            valueDescription = "<all|composableOnly>",
            description = "Controls which functions are scanned for translatable strings.",
            required = false,
            allowMultipleOccurrences = false,
        ),
        CliOption(
            optionName = I18NPluginKeys.useDefaultAnnotationRulesOption,
            valueDescription = "<true|false>",
            description = "Whether to enable built-in annotation i18n whitelist and blacklist rules.",
            required = false,
            allowMultipleOccurrences = false,
        ),
        CliOption(
            optionName = I18NPluginKeys.annotationWhitelistOption,
            valueDescription = "<comma-separated-annotation-names>",
            description = "Whitelisted annotations whose string arguments should be collected into the i18n catalog.",
            required = false,
            allowMultipleOccurrences = false,
        ),
        CliOption(
            optionName = I18NPluginKeys.annotationBlacklistOption,
            valueDescription = "<comma-separated-annotation-names>",
            description = "Blacklisted annotations whose string arguments must be excluded from the i18n catalog.",
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
            I18NPluginKeys.resourceBasePathOption -> {
                configuration.put(I18NPluginKeys.resourceBasePathKey, value)
            }

            I18NPluginKeys.generatedCatalogFileOption -> {
                configuration.put(I18NPluginKeys.generatedCatalogFileKey, value)
            }

            I18NPluginKeys.scanScopeOption -> {
                configuration.put(I18NPluginKeys.scanScopeKey, value)
            }

            I18NPluginKeys.useDefaultAnnotationRulesOption -> {
                configuration.put(I18NPluginKeys.useDefaultAnnotationRulesKey, parseBoolean(value))
            }

            I18NPluginKeys.annotationWhitelistOption -> {
                configuration.put(I18NPluginKeys.annotationWhitelistKey, parseNameList(value))
            }

            I18NPluginKeys.annotationBlacklistOption -> {
                configuration.put(I18NPluginKeys.annotationBlacklistKey, parseNameList(value))
            }

            else -> {
                error("Unknown option: ${option.optionName}")
            }
        }
    }

    private fun parseNameList(value: String): List<String> {
        return value
            .split(',', '\n', '\r')
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
    }

    private fun parseBoolean(value: String): Boolean {
        return value.trim().toBooleanStrictOrNull()
            ?: error(
                "Unsupported boolean option value `$value`. Supported values: `true`, `false`.",
            )
    }
}
