package site.addzero.configcenter.runtime

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import site.addzero.configcenter.spec.ConfigEntryDto
import site.addzero.configcenter.spec.ConfigTargetDto
import site.addzero.configcenter.spec.ConfigTargetKind
import site.addzero.configcenter.spec.ConfigValueType
import site.addzero.configcenter.spec.ConfigRendererSpi

class DefaultConfigRendererSpi(
    private val json: Json,
) : ConfigRendererSpi {
    override fun supports(
        targetKind: ConfigTargetKind,
    ): Boolean {
        return true
    }

    override fun render(
        target: ConfigTargetDto,
        entries: List<ConfigEntryDto>,
    ): String {
        val effectiveEntries = entries
            .filter { it.enabled }
            .sortedBy { it.key }

        return when (target.targetKind) {
            ConfigTargetKind.DOTENV -> renderDotenv(effectiveEntries)
            ConfigTargetKind.OS_ENV_EXPORT -> renderOsEnvExport(effectiveEntries)
            ConfigTargetKind.PROPERTIES_FILE -> renderProperties(effectiveEntries)
            ConfigTargetKind.JSON_FILE -> renderJson(effectiveEntries)
            ConfigTargetKind.SPRING_YAML,
            ConfigTargetKind.YAML_FILE,
            -> renderYaml(effectiveEntries)
            ConfigTargetKind.KTOR_HOCON -> renderHocon(effectiveEntries)
            ConfigTargetKind.DOCKER_COMPOSE_TEMPLATE,
            ConfigTargetKind.DOCKERFILE_TEMPLATE,
            ConfigTargetKind.DOTFILE_TEMPLATE,
            ConfigTargetKind.GENERIC_TEXT_TEMPLATE,
            -> renderTemplate(target.templateText.orEmpty(), effectiveEntries)
        }
    }

    private fun renderDotenv(
        entries: List<ConfigEntryDto>,
    ): String {
        return entries.joinToString("\n") { entry ->
            "${entry.key.toEnvName()}=${entry.value.orEmpty().toEnvValue()}"
        }
    }

    private fun renderOsEnvExport(
        entries: List<ConfigEntryDto>,
    ): String {
        return entries.joinToString("\n") { entry ->
            "export ${entry.key.toEnvName()}=${entry.value.orEmpty().toEnvValue()}"
        }
    }

    private fun renderProperties(
        entries: List<ConfigEntryDto>,
    ): String {
        return entries.joinToString("\n") { entry ->
            "${entry.key}=${entry.value.orEmpty()}"
        }
    }

    private fun renderJson(
        entries: List<ConfigEntryDto>,
    ): String {
        val root = buildNestedStructure(entries).toJsonElement()
        return json.encodeToString(JsonElement.serializer(), root)
    }

    private fun renderYaml(
        entries: List<ConfigEntryDto>,
    ): String {
        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
            indent = 2
        }
        val yaml = Yaml(options)
        return yaml.dump(buildNestedStructure(entries))
    }

    private fun renderHocon(
        entries: List<ConfigEntryDto>,
    ): String {
        return renderHoconMap(
            map = buildNestedStructure(entries),
            indent = 0,
        )
    }

    private fun renderTemplate(
        templateText: String,
        entries: List<ConfigEntryDto>,
    ): String {
        var rendered = templateText
        val flatMap = entries.associate { entry -> entry.key to (entry.value.orEmpty()) }
        flatMap.forEach { (key, value) ->
            rendered = rendered
                .replace("{{${key}}}", value)
                .replace("\${${key}}", value)
        }
        return rendered
    }

    private fun buildNestedStructure(
        entries: List<ConfigEntryDto>,
    ): LinkedHashMap<String, Any?> {
        val root = linkedMapOf<String, Any?>()
        entries.forEach { entry ->
            val segments = entry.key.split(".").filter { it.isNotBlank() }
            if (segments.isEmpty()) {
                return@forEach
            }
            var current = root
            segments.dropLast(1).forEach { segment ->
                val next = current[segment]
                if (next is MutableMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    current = next as LinkedHashMap<String, Any?>
                } else {
                    val child = linkedMapOf<String, Any?>()
                    current[segment] = child
                    current = child
                }
            }
            current[segments.last()] = entry.toTypedValue()
        }
        return root
    }

    private fun renderHoconMap(
        map: Map<String, Any?>,
        indent: Int,
    ): String {
        val pad = " ".repeat(indent)
        return map.entries.joinToString("\n") { (key, value) ->
            if (value is Map<*, *>) {
                buildString {
                    append(pad)
                    append(key)
                    append(" {\n")
                    @Suppress("UNCHECKED_CAST")
                    append(renderHoconMap(value as Map<String, Any?>, indent + 2))
                    append('\n')
                    append(pad)
                    append('}')
                }
            } else {
                "$pad$key = ${value.toHoconValue()}"
            }
        }
    }

    private fun Map<String, Any?>.toJsonElement(): JsonElement {
        return JsonObject(entries.associate { (key, value) ->
            key to value.toJsonElement()
        })
    }

    private fun Any?.toJsonElement(): JsonElement {
        return when (this) {
            null -> JsonNull
            is String -> JsonPrimitive(this)
            is Boolean -> JsonPrimitive(this)
            is Int -> JsonPrimitive(this)
            is Long -> JsonPrimitive(this)
            is Double -> JsonPrimitive(this)
            is Float -> JsonPrimitive(this)
            is JsonElement -> this
            is Map<*, *> -> {
                JsonObject(entries.associate { (key, value) ->
                    key.toString() to value.toJsonElement()
                })
            }

            is Iterable<*> -> JsonArray(map { it.toJsonElement() })
            else -> JsonPrimitive(toString())
        }
    }

    private fun ConfigEntryDto.toTypedValue(): Any? {
        val rawValue = value ?: return null
        return when (valueType) {
            ConfigValueType.BOOLEAN -> rawValue.toBooleanStrictOrNull() ?: rawValue.toBoolean()
            ConfigValueType.INTEGER -> rawValue.toLongOrNull() ?: rawValue
            ConfigValueType.NUMBER -> rawValue.toDoubleOrNull() ?: rawValue
            ConfigValueType.JSON -> {
                runCatching { json.parseToJsonElement(rawValue).toPlainValue() }.getOrElse { rawValue }
            }

            ConfigValueType.STRING,
            ConfigValueType.TEXT,
            -> rawValue
        }
    }

    private fun JsonElement.toPlainValue(): Any? {
        return when (this) {
            is JsonObject -> entries.associate { (key, value) -> key to value.toPlainValue() }
            is JsonArray -> map { it.toPlainValue() }
            is JsonPrimitive -> {
                when {
                    isString -> content
                    booleanOrNull != null -> booleanOrNull
                    longOrNull != null -> longOrNull
                    doubleOrNull != null -> doubleOrNull
                    else -> content
                }
            }

            else -> null
        }
    }

    private fun String.toEnvName(): String {
        return uppercase()
            .replace(Regex("[^A-Z0-9]+"), "_")
            .trim('_')
    }

    private fun String.toEnvValue(): String {
        return "\"${replace("\"", "\\\"")}\""
    }

    private fun Any?.toHoconValue(): String {
        return when (this) {
            null -> "null"
            is String -> "\"${replace("\"", "\\\"")}\""
            is Boolean,
            is Int,
            is Long,
            is Double,
            is Float,
            -> toString()
            is Iterable<*> -> joinToString(prefix = "[", postfix = "]") { item ->
                item.toHoconValue()
            }

            is Map<*, *> -> {
                buildString {
                    append("{ ")
                    append(entries.joinToString(", ") { (key, value) ->
                        "${key.toString()} = ${value.toHoconValue()}"
                    })
                    append(" }")
                }
            }

            else -> "\"${toString().replace("\"", "\\\"")}\""
        }
    }
}

class KtorConfigBridge(
    private val gateway: site.addzero.configcenter.spec.ConfigCenterGateway,
    private val bootstrap: ConfigCenterBootstrap,
) : site.addzero.configcenter.spec.ConfigBridgeSpi {
    override val bridgeName: String = "ktor"

    fun getString(
        key: String,
        defaultValue: String? = null,
        namespace: String = bootstrap.appId,
    ): String? {
        return kotlinx.coroutines.runBlocking {
            gateway.getEnv(
                key = key,
                query = site.addzero.configcenter.spec.ConfigQuery(
                    namespace = namespace,
                    profile = bootstrap.profile,
                ),
            ) ?: defaultValue
        }
    }

    fun getInt(
        key: String,
        defaultValue: Int,
        namespace: String = bootstrap.appId,
    ): Int {
        return getString(key, defaultValue.toString(), namespace)
            ?.toIntOrNull()
            ?: defaultValue
    }
}

fun writeRenderedConfig(
    renderedConfig: site.addzero.configcenter.spec.RenderedConfig,
) {
    if (renderedConfig.outputPath.isBlank()) {
        return
    }
    val outputFile = File(renderedConfig.outputPath).absoluteFile
    outputFile.parentFile?.mkdirs()
    outputFile.writeText(renderedConfig.content)
}

