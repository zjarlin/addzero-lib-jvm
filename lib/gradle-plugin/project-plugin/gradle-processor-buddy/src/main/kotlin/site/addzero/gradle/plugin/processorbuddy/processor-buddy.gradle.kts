package site.addzero.gradle.plugin.processorbuddy

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSetContainer
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
    settingsObjectEnabled.set(extension.settingsObjectEnabled)
    readmeEnabled.set(extension.readmeEnabled)
}

afterEvaluate {
    val outputDir = generateTask.flatMap { it.generatedCodeOutputDir }

    if (extension.settingContextEnabled.get() && extension.mustMap.get().isNotEmpty()) {
        plugins.withId("java") {
            val sourceSets = extensions.getByType(SourceSetContainer::class.java)
            val mainSourceSet = sourceSets.getByName("main")
            mainSourceSet.java.srcDir(outputDir)
            val kotlinSourceDir = mainSourceSet.extensions.findByName("kotlin") as? SourceDirectorySet
            kotlinSourceDir?.srcDir(outputDir)
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
    abstract val settingsObjectEnabled: Property<Boolean>

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
        val mergeFuncName = "merge"

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
import $pkgName.$objName
import $pkgName.$mergeFuncName

// Default singleton
val config: $ifaceName = $objName

// Load config
config.fromOptions(mapOf("key" to "value"))

// Export config
val options = config.toOptions()

// Merge configs (non-empty values win)
val merged = $mergeFuncName(config1, config2)
\`\`\`
        """.trimIndent()
    }

    private fun generateSettingContextInterface(): List<File> {
        if (!settingContextEnabled.get() && !settingsObjectEnabled.get()) {
            logger.lifecycle("SettingContext and Settings generation are disabled")
            return emptyList()
        }

        val outputDir = generatedCodeOutputDir.get().asFile
        val pkgName = packageName.get()
        val ifaceName = interfaceName.get()
        val objName = objectName.get()
        val packageDir = File(outputDir, pkgName.replace(".", "/"))
        packageDir.mkdirs()

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
            val default = toDefaultValueExpression(info.defaultValue, info.type)
            "    override var ${info.name}: ${info.type} = $default"
        }.joinToString("\n")

        val toOptionsBody = propertyInfos.map {
            toSerializationExpression(it.name, it.type)
        }.joinToString(",\n")

        val setFromOptionsBody = propertyInfos.map { info ->
            toFromOptionsExpression(info.name, info.type, info.defaultValue)
        }.joinToString("\n")

        val generatedFiles = mutableListOf<File>()

        // Generate interface
        if (settingContextEnabled.get()) {
            logger.lifecycle("Generating SettingContext interface in: ${packageDir.absolutePath}")
            val file = File(packageDir, "${ifaceName}.kt")

        val mergeFuncName = "merge"
        val topLevelFunctions = if (settingsObjectEnabled.get()) {
            """
            |
            |fun $mergeFuncName(vararg instances: $ifaceName): $ifaceName {
            |    val providedInstances = instances.toList()
            |    if (providedInstances.isEmpty()) {
            |        throw IllegalArgumentException("At least one instance required for merge")
            |    }
            |    if (providedInstances.size == 1) {
            |        return providedInstances[0]
            |    }
            |
            |    val merged = mutableMapOf<String, String>()
            |    providedInstances.forEach {
            |        it.toOptions().forEach { (key, value) ->
            |            if (value.isNotEmpty()) {
            |                merged[key] = value
            |            }
            |        }
            |    }
            |    return $objName.apply { fromOptions(merged) }
            |}"""
            } else {
                ""
            }

            val content = """
            |package $pkgName
            |
            |interface $ifaceName {
            |$propertyDeclarations
            |
            |    fun toOptions(): Map<String, String>
            |
            |    fun fromOptions(options: Map<String, String>)
            |}
            |$topLevelFunctions
        """.trimMargin()

            file.writeText(content)
            generatedFiles.add(file)
            logger.lifecycle("Generated SettingContext interface to: ${file.absolutePath}")
        } else {
            logger.lifecycle("SettingContext interface generation is disabled")
        }

        // Generate settings object
        if (settingsObjectEnabled.get()) {
            logger.lifecycle("Generating Settings object in: ${packageDir.absolutePath}")
            val file = File(packageDir, "${objName}.kt")

            val implementsClause = if (settingContextEnabled.get()) {
                " : $ifaceName"
            } else {
                ""
            }

            val overrideModifier = if (settingContextEnabled.get()) {
                "override "
            } else {
                ""
            }

            val toOptionsSignature = if (settingContextEnabled.get()) {
                "override fun toOptions(): Map<String, String> = mapOf("
            } else {
                "fun toOptions(): Map<String, String> = mapOf("
            }

            val fromOptionsSignature = if (settingContextEnabled.get()) {
                "override fun fromOptions(options: Map<String, String>) {"
            } else {
                "fun fromOptions(options: Map<String, String>) {"
            }

            val objProperties = if (settingContextEnabled.get()) {
                implProperties
            } else {
                propertyInfos.map { info ->
                    val default = toDefaultValueExpression(info.defaultValue, info.type)
                    "    ${overrideModifier}var ${info.name}: ${info.type} = $default"
                }.joinToString("\n")
            }

            val content = """
            |package $pkgName
            |
            |object $objName$implementsClause {
            |$objProperties
            |
            |    $toOptionsSignature
            |$toOptionsBody
            |    )
            |
            |    $fromOptionsSignature
            |$setFromOptionsBody
            |    }
            |}
        """.trimMargin()

            file.writeText(content)
            generatedFiles.add(file)
            logger.lifecycle("Generated Settings object to: ${file.absolutePath}")
        } else {
            logger.lifecycle("Settings object generation is disabled")
        }

        return generatedFiles
    }

    private fun inferType(value: String): String {
        // 优先检测列表类型
        val listType = detectListType(value)
        if (listType != null) {
            return listType.second
        }
        return when {
            value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true) -> "Boolean"
            value.toIntOrNull() != null && !value.startsWith("0") && !value.contains(".") -> "Int"
            value.toLongOrNull() != null && !value.contains(".") -> "Long"
            value.toDoubleOrNull() != null && value.contains(".") -> "Double"
            else -> "String"
        }
    }

    /**
     * 检测值是否为逗号分隔的列表，并推断列表元素类型
     */
    private fun detectListType(value: String): Pair<Boolean, String>? {
        if (!value.contains(",") || value.startsWith("\"")) {
            return null
        }
        val parts = value.split(",").map { it.trim() }
        if (parts.size < 2) return null

        // 检测每个部分的类型是否一致
        val elementType = when {
            parts.all { it.equals("true", ignoreCase = true) || it.equals("false", ignoreCase = true) } -> "Boolean"
            parts.all { it.toIntOrNull() != null && !it.startsWith("0") && !it.contains(".") } -> "Int"
            parts.all { it.toLongOrNull() != null && !it.contains(".") } -> "Long"
            parts.all { it.toDoubleOrNull() != null } -> "Double"
            else -> "String"
        }
        return true to "List<$elementType>"
    }

    /**
     * 将字符串值转换为 Kotlin 代码中的默认值表达式
     */
    private fun toDefaultValueExpression(value: String, type: String): String {
        val listType = detectListType(value)
        return if (listType != null && type.startsWith("List<")) {
            val (isList, listTypeStr) = listType
            val elementType = listTypeStr.removePrefix("List<").removeSuffix(">")
            val parts = value.split(",").map { it.trim() }
            val elementValues = when (elementType) {
                "String" -> parts.joinToString(", ") { "\"$it\"" }
                "Int" -> parts.joinToString(", ") { it }
                "Long" -> parts.joinToString(", ") { "${it}L" }
                "Double" -> parts.joinToString(", ") { it }
                "Boolean" -> parts.joinToString(", ") { it.lowercase() }
                else -> parts.joinToString(", ")
            }
            "listOf($elementValues)"
        } else {
            when (type) {
                "Boolean" -> value.toBoolean().toString()
                "Int" -> (value.toIntOrNull() ?: 0).toString()
                "Long" -> "${value.toLongOrNull() ?: 0L}L"
                "Double" -> "${value.toDoubleOrNull() ?: 0.0}"
                else -> "\"$value\""
            }
        }
    }

    /**
     * 将类型转换为可空类型用于 toOptions 序列化
     */
    private fun toSerializationExpression(propertyName: String, type: String): String {
        return if (type.startsWith("List<")) {
            """        "$propertyName" to ${propertyName}.joinToString(",")"""
        } else {
            """        "$propertyName" to ${propertyName}.toString()"""
        }
    }

    /**
     * 生成 fromOptions 中的赋值语句
     */
    private fun toFromOptionsExpression(propertyName: String, type: String, defaultValue: String): String {
        val defaultExpr = toDefaultValueExpression(defaultValue, type)
        return if (type.startsWith("List<")) {
            val elementType = type.removePrefix("List<").removeSuffix(">")
            val parseExpr = when (elementType) {
                "String" -> "it"
                "Int" -> "it.toIntOrNull()"
                "Long" -> "it.toLongOrNull()"
                "Double" -> "it.toDoubleOrNull()"
                "Boolean" -> "it.toBoolean()"
                else -> "it"
            }
            val filterExpr = if (elementType != "String") "?.filterNotNull()" else ""
            """        this.$propertyName = options["$propertyName"]?.split(",")?.map { $parseExpr }$filterExpr ?: $defaultExpr"""
        } else {
            when (type) {
                "Boolean" -> """        this.$propertyName = options["$propertyName"]?.toBoolean() ?: $defaultExpr"""
                "Int" -> """        this.$propertyName = options["$propertyName"]?.toIntOrNull() ?: $defaultExpr"""
                "Long" -> """        this.$propertyName = options["$propertyName"]?.toLongOrNull() ?: $defaultExpr"""
                "Double" -> """        this.$propertyName = options["$propertyName"]?.toDoubleOrNull() ?: $defaultExpr"""
                else -> """        this.$propertyName = options["$propertyName"] ?: $defaultExpr"""
            }
        }
    }
}
