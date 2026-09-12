package site.addzero.toolchain.processorsettings

import org.jetbrains.amper.plugins.Configurable
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.writeText

@Configurable
interface ProcessorSettingsBuildSettings {
    val packageName: String
    val properties: Map<String, String> get() = emptyMap()
    val style: String get() = "context"
    val settingsClassName: String get() = "Settings"
    val contextClassName: String get() = "SettingContext"
}

private enum class PropertyKind {
    Boolean,
    Int,
    Double,
    String,
    StringList,
}

private data class GeneratedProperty(
    val optionKey: String,
    val propertyName: String,
    val defaultValue: String,
    val kind: PropertyKind,
)

@TaskAction
@OptIn(ExperimentalPathApi::class)
fun generateProcessorSettings(
    packageName: String,
    properties: Map<String, String>,
    style: String,
    settingsClassName: String,
    contextClassName: String,
    @Output generatedSourceDirectory: Path,
) {
    require(packageName.substringAfterLast('.') == "generated") {
        "Generated processor settings package must end with '.generated': $packageName"
    }

    val generatedProperties = properties.map { (optionKey, defaultValue) ->
        GeneratedProperty(
            optionKey = optionKey,
            propertyName = optionKey.toPropertyName(),
            defaultValue = defaultValue,
            kind = defaultValue.inferKind(),
        )
    }

    generatedSourceDirectory.deleteRecursively()
    val packageDirectory = generatedSourceDirectory / packageName.replace('.', '/')
    packageDirectory.createDirectories()

    when (style) {
        "context" -> generateContextStyle(
            packageName = packageName,
            settingsClassName = settingsClassName,
            contextClassName = contextClassName,
            properties = generatedProperties,
            packageDirectory = packageDirectory,
        )

        "holder" -> generateHolderStyle(
            packageName = packageName,
            settingsClassName = settingsClassName,
            contextClassName = contextClassName,
            properties = generatedProperties,
            packageDirectory = packageDirectory,
        )

        else -> error("Unsupported processor settings style: $style")
    }
}

private fun generateContextStyle(
    packageName: String,
    settingsClassName: String,
    contextClassName: String,
    properties: List<GeneratedProperty>,
    packageDirectory: Path,
) {
    val declarations = properties.joinToString("\n") { property ->
        "    override var ${property.propertyName}: ${property.kind.kotlinType()} = ${property.defaultExpression()}"
    }
    val optionMappings = properties.joinToString(",\n") { property ->
        "        \"${property.optionKey}\" to ${property.toOptionExpression()}"
    }
    val optionAssignments = properties.joinToString("\n") { property ->
        "        this.${property.propertyName} = ${property.fromOptionExpression()}"
    }
    val interfaceProperties = properties.joinToString("\n") { property ->
        "    var ${property.propertyName}: ${property.kind.kotlinType()}"
    }

    (packageDirectory / "$settingsClassName.kt").writeText(
        """
        package $packageName

        object $settingsClassName : $contextClassName {
        $declarations

            override fun toOptions(): Map<String, String> = mapOf(
        $optionMappings
            )

            override fun fromOptions(options: Map<String, String>) {
        $optionAssignments
            }
        }
        """.trimIndent() + "\n",
    )

    (packageDirectory / "$contextClassName.kt").writeText(
        """
        package $packageName

        interface $contextClassName {
        $interfaceProperties

            fun toOptions(): Map<String, String>

            fun fromOptions(options: Map<String, String>)
        }

        fun merge(vararg instances: $contextClassName): $contextClassName {
            require(instances.isNotEmpty()) { "At least one instance required for merge" }
            if (instances.size == 1) {
                return instances[0]
            }

            val merged = mutableMapOf<String, String>()
            instances.forEach { instance ->
                instance.toOptions().forEach { (key, value) ->
                    if (value.isNotEmpty()) {
                        merged[key] = value
                    }
                }
            }
            return $settingsClassName.apply { fromOptions(merged) }
        }
        """.trimIndent() + "\n",
    )
}

private fun generateHolderStyle(
    packageName: String,
    settingsClassName: String,
    contextClassName: String,
    properties: List<GeneratedProperty>,
    packageDirectory: Path,
) {
    val declarations = properties.joinToString("\n") { property ->
        "    var ${property.propertyName}: String = ${property.defaultValue.kotlinString()}"
    }
    val assignments = properties.joinToString("\n") { property ->
        "        settings.${property.propertyName} = options[\"${property.optionKey}\"] ?: ${property.defaultValue.kotlinString()}"
    }

    (packageDirectory / "$contextClassName.kt").writeText(
        """
        package $packageName

        class $contextClassName {
        $declarations
        }
        """.trimIndent() + "\n",
    )

    (packageDirectory / "$settingsClassName.kt").writeText(
        """
        package $packageName

        import java.util.concurrent.atomic.AtomicReference

        object $settingsClassName {
            private val settingsRef = AtomicReference<$contextClassName>()

            fun getSettings(): $contextClassName = settingsRef.get() ?: $contextClassName()

            fun initialize(options: Map<String, String>) {
                val settings = $contextClassName()
        $assignments
                settingsRef.compareAndSet(null, settings)
            }
        }
        """.trimIndent() + "\n",
    )
}

private fun String.toPropertyName(): String {
    val parts = split(Regex("[^A-Za-z0-9]+"))
        .filter(String::isNotBlank)
    require(parts.isNotEmpty()) { "Processor option key cannot be empty" }

    return buildString {
        val first = parts.first()
        append(if (parts.size == 1) first.replaceFirstChar(Char::lowercase) else first)
        parts.drop(1).forEach { part ->
            append(part.replaceFirstChar(Char::uppercase))
        }
    }
}

private fun String.inferKind(): PropertyKind = when {
    startsWith("listOf(") || this == "," || contains(',') -> PropertyKind.StringList
    equals("true", ignoreCase = true) || equals("false", ignoreCase = true) -> PropertyKind.Boolean
    matches(Regex("-?\\d+")) -> PropertyKind.Int
    matches(Regex("-?\\d+\\.\\d+")) -> PropertyKind.Double
    else -> PropertyKind.String
}

private fun PropertyKind.kotlinType(): String = when (this) {
    PropertyKind.Boolean -> "Boolean"
    PropertyKind.Int -> "Int"
    PropertyKind.Double -> "Double"
    PropertyKind.String -> "String"
    PropertyKind.StringList -> "List<String>"
}

private fun GeneratedProperty.defaultExpression(): String = when (kind) {
    PropertyKind.Boolean, PropertyKind.Int, PropertyKind.Double -> defaultValue
    PropertyKind.String -> defaultValue.kotlinString()
    PropertyKind.StringList -> defaultValue.listDefaultExpression()
}

private fun GeneratedProperty.toOptionExpression(): String = when (kind) {
    PropertyKind.StringList -> "$propertyName.joinToString(\",\")"
    else -> "$propertyName.toString()"
}

private fun GeneratedProperty.fromOptionExpression(): String = when (kind) {
    PropertyKind.Boolean -> "options[\"$optionKey\"]?.toBoolean() ?: $defaultValue"
    PropertyKind.Int -> "options[\"$optionKey\"]?.toIntOrNull() ?: $defaultValue"
    PropertyKind.Double -> "options[\"$optionKey\"]?.toDoubleOrNull() ?: $defaultValue"
    PropertyKind.String -> "options[\"$optionKey\"] ?: ${defaultValue.kotlinString()}"
    PropertyKind.StringList ->
        "options[\"$optionKey\"]?.split(\",\")?.filter(String::isNotEmpty) ?: ${defaultValue.listDefaultExpression()}"
}

private fun String.listDefaultExpression(): String {
    if (this == "," || isEmpty()) {
        return "emptyList()"
    }
    if (startsWith("listOf(") && endsWith(')')) {
        return this
    }
    return split(',').joinToString(prefix = "listOf(", postfix = ")") { item ->
        item.trim().kotlinString()
    }
}

private fun String.kotlinString(): String = buildString {
    append('"')
    this@kotlinString.forEach { character ->
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(character)
        }
    }
    append('"')
}
