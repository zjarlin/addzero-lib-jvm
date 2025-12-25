package site.addzero.gradle.plugin.processorbuddy

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

val extension = extensions.create<ProcessorBuddyExtension>("processorBuddy")

val generateTask = tasks.register<GenerateProcessorScriptTask>("generateProcessorScript") {
    description = "Generates processor configuration based on mustMap"
    group = "build"

    readmeOutputFile = File(project.projectDir, "README.md")

    val buildOutputDir = project.layout.buildDirectory.dir("generated/processor-buddy")
    generatedCodeOutputDir.set(buildOutputDir)

    mustMap.set(extension.mustMap)
    interfaceName.set(extension.interfaceName)
    objectName.set(extension.objectName)
    packageName.set(extension.packageName)
    settingContextEnabled.set(extension.settingContextEnabled)
    readmeEnabled.set(extension.readmeEnabled)
}

afterEvaluate {
    val outputDir = generateTask.flatMap { it.generatedCodeOutputDir }

    if (extension.settingContextEnabled.get() && extension.mustMap.get().isNotEmpty()) {
        plugins.withId("java") {
            val sourceSets = extensions.getByType(SourceSetContainer::class.java)
            sourceSets.getByName("main").java.srcDir(outputDir)
            logger.lifecycle("Added generated source directory to main sourceSet: ${outputDir.get().asFile.absolutePath}")
        }
    }

    tasks.findByName("compileKotlin")?.dependsOn(generateTask)
    tasks.findByName("compileJava")?.dependsOn(generateTask)

    tasks.matching { task -> task.name == "ideaSyncTask" || task.name.endsWith("SyncTask") }
        .forEach { it.dependsOn(generateTask) }

    gradle.taskGraph.whenReady {
        val isSync = gradle.taskGraph.allTasks.any { task -> task.name == "ideaSyncTask" || task.name.endsWith("SyncTask") }
        if (isSync) {
            generateTask.get()
        }
    }
}

abstract class GenerateProcessorScriptTask : DefaultTask() {
    @get:Input
    abstract val mustMap: MapProperty<String, String>

    @get:Input
    abstract val interfaceName: Property<String>

    @get:Input
    abstract val objectName: Property<String>

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val settingContextEnabled: Property<Boolean>

    @get:Input
    abstract val readmeEnabled: Property<Boolean>

    @get:Internal
    abstract var readmeOutputFile: File

    @get:OutputDirectory
    abstract val generatedCodeOutputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        logger.lifecycle("ProcessorBuddy: Starting code generation...")
        logger.lifecycle("ProcessorBuddy: mustMap contains ${mustMap.get().size} entries")

        if (readmeEnabled.get()) {
            readmeOutputFile.writeText(generateReadme())
            logger.lifecycle("Generated README.md to: ${readmeOutputFile.absolutePath}")
        } else {
            logger.lifecycle("ProcessorBuddy: README.md generation is disabled")
        }

        val generatedFiles = generateSettingContextInterface()
        logger.lifecycle("ProcessorBuddy: Generated ${generatedFiles.size} configuration files")
    }

    private fun generateReadme(): String {
        val properties = mustMap.get()
        val ifaceName = interfaceName.get()
        val objName = objectName.get()
        val pkgName = packageName.get()

        return """
# Processor Configuration

## Consumer Usage

### Gradle (KSP)

\`\`\`kotlin
plugins {
    id("com.google.devtools.ksp")
}

ksp {
${properties.map { (key, value) -> """    arg("$key", "$value")""" }.joinToString("\n")}
}
\`\`\`

---

### Maven (APT)

\`\`\`xml
<properties>
${properties.map { (key, value) -> """    <apt.$key>$value</apt.$key>""" }.joinToString("\n")}
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <compilerArgs>
${properties.map { (key, _) -> """                    <arg>-A$key=$""" + """{apt.$key}</arg>""" }.joinToString("\n")}
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
\`\`\`

---

## Library Author Usage

Apply the plugin in your `build.gradle.kts`:

\`\`\`kotlin
plugins {
    id("site.addzero.gradle.plugin.processorbuddy")
}

processorBuddy {
    mustMap.set(mapOf(
${properties.map { (key, value) -> """        "$key" to "$value"""" }.joinToString(",\n")}
    ))
}
\`\`\`

This generates:
- Interface: `$pkgName.$ifaceName`
- Singleton: `$pkgName.$objName`

\`\`\`kotlin
import $pkgName.$ifaceName

// Get singleton
val config = $ifaceName.instance()

// Load config
config.fromOptions(mapOf("key" to "value"))

// Export config
val options = config.toOptions()

// Merge configs (non-empty values win)
val merged = $ifaceName.merge(config1, config2)
\`\`\`
        """.trimIndent()
    }

    private fun generateSettingContextInterface(): List<File> {
        if (!settingContextEnabled.get()) {
            logger.lifecycle("SettingContext generation is disabled")
            return emptyList()
        }

        val outputDir = generatedCodeOutputDir.get().asFile
        val pkgName = packageName.get()
        val ifaceName = interfaceName.get()
        val objName = objectName.get()
        val packageDir = File(outputDir, pkgName.replace(".", "/"))
        packageDir.mkdirs()

        logger.lifecycle("Generating SettingContext interface in: ${packageDir.absolutePath}")

        val file = File(packageDir, "${ifaceName}.kt")
        val properties = mustMap.get()

        data class PropertyInfo(val name: String, val defaultValue: String, val type: String)

        val propertyInfos = properties.map { (key, value) ->
            val type = inferType(value)
            PropertyInfo(key, value, type)
        }

        val propertyDeclarations = propertyInfos.map {
            "    var ${it.name}: ${it.type}"
        }.joinToString("\n")

        val implProperties = propertyInfos.map { info ->
            val default = when (info.type) {
                "Boolean" -> info.defaultValue.toBoolean()
                "Int" -> info.defaultValue.toIntOrNull() ?: 0
                "Long" -> info.defaultValue.toLongOrNull() ?: 0L
                "Double" -> info.defaultValue.toDoubleOrNull() ?: 0.0
                else -> "\"${info.defaultValue}\""
            }
            "    override var ${info.name}: ${info.type} = $default"
        }.joinToString("\n")

        val toOptionsBody = propertyInfos.map {
            """        "${it.name}" to ${it.name}.toString()"""
        }.joinToString(",\n")

        val setFromOptionsBody = propertyInfos.map { info ->
            when (info.type) {
                "Boolean" -> """        this.${info.name} = options["${info.name}"]?.toBoolean() ?: ${info.defaultValue.toBoolean()}"""
                "Int" -> """        this.${info.name} = options["${info.name}"]?.toIntOrNull() ?: ${info.defaultValue.toIntOrNull() ?: 0}"""
                "Long" -> """        this.${info.name} = options["${info.name}"]?.toLongOrNull() ?: ${info.defaultValue.toLongOrNull() ?: 0L}"""
                "Double" -> """        this.${info.name} = options["${info.name}"]?.toDoubleOrNull() ?: ${info.defaultValue.toDoubleOrNull() ?: 0.0}"""
                else -> """        this.${info.name} = options["${info.name}"] ?: "${info.defaultValue}""""
            }
        }.joinToString("\n")

        val content = """
            |package $pkgName
            |
            |/**
            | * Processor configuration interface.
            | * Generated by ProcessorBuddy.
            | */
            |interface $ifaceName {
            |$propertyDeclarations
            |
            |    /**
            |     * Exports current properties to a Map.
            |     */
            |    fun toOptions(): Map<String, String>
            |
            |    /**
            |     * Loads properties from a Map.
            |     */
            |    fun fromOptions(options: Map<String, String>)
            |
            |    companion object {
            |        /**
            |         * Returns the default singleton instance.
            |         */
            |        @JvmStatic
            |        fun instance(): $ifaceName = $objName
            |
            |        /**
            |         * Merges multiple $ifaceName instances.
            |         * Later instances override earlier ones, but only for non-empty String values.
            |         */
            |        @JvmStatic
            |        fun merge(vararg instances: $ifaceName?): $ifaceName {
            |            val nonNullInstances = instances.filterNotNull()
            |            if (nonNullInstances.isEmpty()) {
            |                throw IllegalArgumentException("At least one non-null instance required for merge")
            |            }
            |            if (nonNullInstances.size == 1) {
            |                return nonNullInstances[0]
            |            }
            |
            |            val merged = mutableMapOf<String, String>()
            |            nonNullInstances.forEach {
            |                it.toOptions().forEach { (key, value) ->
            |                    if (value.isNotEmpty()) {
            |                        merged[key] = value
            |                    }
            |                }
            |            }
            |            return $objName.apply { fromOptions(merged) }
            |        }
            |    }
            |}
            |
            |/**
            | * Default implementation of $ifaceName.
            | * Generated by ProcessorBuddy.
            | */
            |object $objName : $ifaceName {
            |$implProperties
            |
            |    override fun toOptions(): Map<String, String> = mapOf(
            |$toOptionsBody
            |    )
            |
            |    override fun fromOptions(options: Map<String, String>) {
            |$setFromOptionsBody
            |    }
            |}
        """.trimMargin()

        file.writeText(content)
        logger.lifecycle("Generated SettingContext interface to: ${file.absolutePath}")
        return listOf(file)
    }

    private fun inferType(value: String): String {
        return when {
            value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true) -> "Boolean"
            value.toIntOrNull() != null && !value.startsWith("0") && !value.contains(".") -> "Int"
            value.toLongOrNull() != null && !value.contains(".") -> "Long"
            value.toDoubleOrNull() != null && value.contains(".") -> "Double"
            else -> "String"
        }
    }
}
