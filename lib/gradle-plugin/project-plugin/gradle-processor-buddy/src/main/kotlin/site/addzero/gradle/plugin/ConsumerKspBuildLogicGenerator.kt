package site.addzero.gradle.plugin

import site.addzero.util.str.toLowCamelCase
import site.addzero.util.str.toBigCamelCase

data class ConsumerKspCompanionDependencySpec(
    val scope: String,
    val artifactKind: String,
    val localProjectPath: String?,
    val artifactId: String?,
    val notation: String?,
)

data class ConsumerKspPropertySpec(
    val optionKey: String,
    val propertyName: String,
    val defaultValue: String,
    val kotlinType: String,
    val alwaysEmit: Boolean,
)

data class ConsumerKspBuildLogicSpec(
    val scriptPackageName: String,
    val scriptName: String,
    val extensionName: String,
    val extensionClassName: String,
    val generatedPackageName: String,
    val pluginId: String,
    val coordinatesResourcePath: String,
    val serializedArgsPropertyName: String,
    val processorProjectPath: String,
    val processorArtifactId: String,
    val processorArtifactKind: String,
    val companionDependencies: List<ConsumerKspCompanionDependencySpec>,
    val properties: List<ConsumerKspPropertySpec>,
)

object ConsumerKspBuildLogicGenerator {

    fun buildSpec(
        mustMap: Map<String, String>,
        scriptPackageName: String,
        scriptName: String,
        extensionName: String,
        processorProjectPath: String,
        processorArtifactId: String,
        processorArtifactKind: String,
        companionDependencies: List<String>,
    ): ConsumerKspBuildLogicSpec {
        val normalizedScriptName = scriptName.removeSuffix(".gradle.kts").trim()
        require(normalizedScriptName.isNotBlank()) {
            "consumerKspBuildLogicScriptName 不能为空。"
        }

        val normalizedPackageName = scriptPackageName.trim().trimEnd('.')
        require(normalizedPackageName.isNotBlank()) {
            "consumerKspBuildLogicPackageName 不能为空。"
        }

        val normalizedExtensionName = extensionName.ifBlank { normalizedScriptName.toLowCamelCase() }
        val extensionClassName = normalizedExtensionName.toBigCamelCase() + "Extension"
        val generatedPackageName = "$normalizedPackageName.generated"
        val pluginId = "$normalizedPackageName.$normalizedScriptName"
        val coordinatesResourcePath = pluginId.replace(".", "/") + "/gradle-plugin.properties"
        val serializedArgsPropertyName = "site.addzero.kspconsumer.$pluginId.serializedArgs"
        val prefixToStrip = detectCommonPrefixToStrip(mustMap.keys)

        val properties = mustMap.map { (key, value) ->
            val kotlinType = CodeGenHelper.inferType(value)
            val propertyName = derivePropertyName(
                optionKey = key,
                prefixToStrip = prefixToStrip,
                kotlinType = kotlinType,
            )
            ConsumerKspPropertySpec(
                optionKey = key,
                propertyName = propertyName,
                defaultValue = value,
                kotlinType = kotlinType,
                alwaysEmit = shouldAlwaysEmit(value, kotlinType),
            )
        }

        return ConsumerKspBuildLogicSpec(
            scriptPackageName = normalizedPackageName,
            scriptName = normalizedScriptName,
            extensionName = normalizedExtensionName,
            extensionClassName = extensionClassName,
            generatedPackageName = generatedPackageName,
            pluginId = pluginId,
            coordinatesResourcePath = coordinatesResourcePath,
            serializedArgsPropertyName = serializedArgsPropertyName,
            processorProjectPath = processorProjectPath,
            processorArtifactId = processorArtifactId,
            processorArtifactKind = processorArtifactKind.uppercase(),
            companionDependencies = companionDependencies.map(::parseCompanionDependency),
            properties = properties,
        )
    }

    fun generateExtensionFile(spec: ConsumerKspBuildLogicSpec): String {
        val imports = linkedSetOf(
            "org.gradle.api.provider.Property",
        ).apply {
            if (spec.properties.any { it.kotlinType.startsWith("List<") }) {
                add("org.gradle.api.provider.ListProperty")
            }
            if (spec.properties.any { it.kotlinType.startsWith("Set<") }) {
                add("org.gradle.api.provider.SetProperty")
            }
        }.joinToString("\n") { "import $it" }

        val propertyDeclarations = spec.properties.joinToString("\n") { property ->
            "    abstract val ${property.propertyName}: ${gradlePropertyType(property.kotlinType)}"
        }

        val conventions = spec.properties.joinToString("\n") { property ->
            val defaultExpression = CodeGenHelper.toDefaultValueExpression(property.defaultValue, property.kotlinType)
            "        ${property.propertyName}.convention($defaultExpression)"
        }

        val argStatements = spec.properties.joinToString("\n") { property ->
            renderArgStatement(property)
        }

        return """
            |package ${spec.generatedPackageName}
            |
            |$imports
            |
            |// 由 ProcessorBuddy 生成，请勿手改。
            |abstract class ${spec.extensionClassName} {
            |$propertyDeclarations
            |
            |    init {
            |$conventions
            |    }
            |}
            |
            |// 由 ProcessorBuddy 生成，请勿手改。
            |fun collect${spec.extensionClassName}KspArgs(
            |    extension: ${spec.extensionClassName},
            |): LinkedHashMap<String, String> =
            |    linkedMapOf<String, String>().apply {
            |$argStatements
            |    }
            |""".trimMargin()
    }

    fun generateScriptFile(spec: ConsumerKspBuildLogicSpec): String {
        val companionDeclarations = spec.companionDependencies.joinToString(",\n") { dependency ->
            """
            |    GeneratedPublishedDependency(
            |        scope = "${dependency.scope}",
            |        artifactKind = "${dependency.artifactKind}",
            |        localProjectPath = ${dependency.localProjectPath?.let { "\"$it\"" } ?: "null"},
            |        artifactId = ${dependency.artifactId?.let { "\"$it\"" } ?: "null"},
            |        notation = ${dependency.notation?.let { "\"$it\"" } ?: "null"},
            |    )""".trimMargin()
        }

        val companionListExpression = if (companionDeclarations.isBlank()) {
            "emptyList()"
        } else {
            "listOf(\n$companionDeclarations\n)"
        }

        return """
            |package ${spec.scriptPackageName}
            |
            |import com.google.devtools.ksp.gradle.KspAATask
            |import com.google.devtools.ksp.gradle.KspExtension
            |import org.gradle.api.Project
            |import org.gradle.api.Task
            |import org.gradle.api.artifacts.dsl.DependencyHandler
            |import org.gradle.kotlin.dsl.create
            |import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
            |import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
            |import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
            |import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
            |import site.addzero.ksp.generated.${spec.extensionClassName}
            |import site.addzero.ksp.generated.collect${spec.extensionClassName}KspArgs
            |import java.util.Properties
            |
            |private const val GENERATED_SERIALIZED_ARGS_PROPERTY = "${spec.serializedArgsPropertyName}"
            |private const val JVM_KSP_GENERATED_DIR = "build/generated/ksp/main/kotlin"
            |private const val KMP_COMMON_KSP_GENERATED_DIR = "build/generated/ksp/metadata/commonMain/kotlin"
            |private const val KMP_JVM_KSP_GENERATED_DIR = "build/generated/ksp/jvm/jvmMain/kotlin"
            |
            |private data class GeneratedPublishedArtifact(
            |    val artifactKind: String,
            |    val localProjectPath: String?,
            |    val artifactId: String,
            |)
            |
            |private data class GeneratedPublishedDependency(
            |    val scope: String,
            |    val artifactKind: String,
            |    val localProjectPath: String?,
            |    val artifactId: String?,
            |    val notation: String?,
            |)
            |
            |private val generatedProcessorArtifact =
            |    GeneratedPublishedArtifact(
            |        artifactKind = "${spec.processorArtifactKind}",
            |        localProjectPath = "${spec.processorProjectPath}",
            |        artifactId = "${spec.processorArtifactId}",
            |    )
            |
            |private val generatedCompanionDependencies =
            |    $companionListExpression
            |
            |private fun loadGeneratedCoordinates(classLoader: ClassLoader): Pair<String, String> {
            |    val resource =
            |        classLoader.getResourceAsStream("${spec.coordinatesResourcePath}")
            |            ?: error("缺少坐标资源: ${spec.coordinatesResourcePath}")
            |    val properties = Properties()
            |    resource.use(properties::load)
            |    val groupId =
            |        properties.getProperty("groupId")
            |            ?: error("缺少 groupId: ${spec.coordinatesResourcePath}")
            |    val version =
            |        properties.getProperty("version")
            |            ?: error("缺少 version: ${spec.coordinatesResourcePath}")
            |    return groupId to version
            |}
            |
            |private fun processorConfigurationName(
            |    artifactKind: String,
            |    kotlinMultiplatform: Boolean,
            |): String =
            |    if (!kotlinMultiplatform) {
            |        "ksp"
            |    } else {
            |        when (artifactKind.uppercase()) {
            |            "KMP" -> "kspCommonMainMetadata"
            |            else -> "kspJvm"
            |        }
            |    }
            |
            |private fun companionConfigurationName(
            |    scope: String,
            |    artifactKind: String,
            |    kotlinMultiplatform: Boolean,
            |): String? {
            |    val prefix =
            |        if (!kotlinMultiplatform) {
            |            ""
            |        } else {
            |            when (artifactKind.uppercase()) {
            |                "KMP" -> "commonMain"
            |                else -> "jvmMain"
            |            }
            |        }
            |    val suffix =
            |        when (scope.uppercase()) {
            |            "IMPLEMENTATION" -> "Implementation"
            |            "API" -> "Api"
            |            "COMPILE_ONLY" -> "CompileOnly"
            |            else -> return null
            |        }
            |    return if (prefix.isBlank()) {
            |        suffix.replaceFirstChar(Char::lowercase)
            |    } else {
            |        prefix + suffix
            |    }
            |}
            |
            |private fun DependencyHandler.addGeneratedDependency(
            |    project: Project,
            |    configurationName: String,
            |    localProjectPath: String?,
            |    artifactId: String?,
            |    notation: String?,
            |    groupId: String,
            |    version: String,
            |) {
            |    if (project.configurations.findByName(configurationName) == null) {
            |        return
            |    }
            |
            |    when {
            |        localProjectPath != null && project.rootProject.findProject(localProjectPath) != null -> {
            |            add(configurationName, project.project(localProjectPath))
            |        }
            |
            |        notation != null -> {
            |            add(configurationName, notation)
            |        }
            |
            |        artifactId != null -> {
            |            add(configurationName, "${'$'}groupId:${'$'}artifactId:${'$'}version")
            |        }
            |
            |        else -> {
            |            error("依赖配置缺少 artifactId/notion/localProjectPath")
            |        }
            |    }
            |}
            |
            |private fun Project.configureGeneratedJvmConsumer() {
            |    extensions.findByType(KotlinJvmProjectExtension::class.java)
            |        ?.sourceSets
            |        ?.findByName("main")
            |        ?.kotlin
            |        ?.srcDir(JVM_KSP_GENERATED_DIR)
            |
            |    tasks.withType(KotlinCompile::class.java).configureEach {
            |        if (name != "kspKotlin") {
            |            dependsOn("kspKotlin")
            |        }
            |    }
            |
            |    tasks.configureEach {
            |        if (name == "sourcesJar") {
            |            dependsOn("kspKotlin")
            |        }
            |    }
            |}
            |
            |private fun Project.configureGeneratedKmpConsumer() {
            |    extensions.findByType(KotlinMultiplatformExtension::class.java)?.let { kotlin ->
            |        kotlin.sourceSets.findByName("commonMain")?.kotlin?.srcDir(KMP_COMMON_KSP_GENERATED_DIR)
            |        kotlin.sourceSets.findByName("jvmMain")?.kotlin?.srcDir(KMP_JVM_KSP_GENERATED_DIR)
            |    }
            |
            |    val commonMainTaskName = "kspCommonMainKotlinMetadata"
            |
            |    fun Task.dependsOnCommonMainIfPresent() {
            |        if (name == commonMainTaskName) {
            |            return
            |        }
            |        if (tasks.names.contains(commonMainTaskName)) {
            |            dependsOn(commonMainTaskName)
            |        }
            |    }
            |
            |    tasks.withType(KotlinCompile::class.java).configureEach {
            |        dependsOnCommonMainIfPresent()
            |        if (name == "compileKotlinJvm" && tasks.names.contains("kspKotlinJvm")) {
            |            dependsOn("kspKotlinJvm")
            |        }
            |    }
            |    tasks.withType(Kotlin2JsCompile::class.java).configureEach {
            |        dependsOnCommonMainIfPresent()
            |    }
            |    tasks.withType(KspAATask::class.java).configureEach {
            |        dependsOnCommonMainIfPresent()
            |    }
            |    tasks.configureEach {
            |        if (name.contains("sourcesJar") || name == "jvmJar") {
            |            dependsOnCommonMainIfPresent()
            |        }
            |        if (name in setOf("jvmJar", "jvmSourcesJar") && tasks.names.contains("kspKotlinJvm")) {
            |            dependsOn("kspKotlinJvm")
            |        }
            |    }
            |}
            |
            |// 由 ProcessorBuddy 生成，请勿手改。
            |val generatedKspResourceClassLoader =
            |    Thread.currentThread().contextClassLoader ?: javaClass.classLoader
            |
            |// 由 ProcessorBuddy 生成，请勿手改。
            |val ${spec.extensionName}Extension = extensions.create<${spec.extensionClassName}>("${spec.extensionName}")
            |
            |pluginManager.apply("com.google.devtools.ksp")
            |
            |afterEvaluate {
            |    val args = collect${spec.extensionClassName}KspArgs(${spec.extensionName}Extension)
            |    extensions.extraProperties.set(GENERATED_SERIALIZED_ARGS_PROPERTY, args)
            |    if (args.isEmpty()) {
            |        return@afterEvaluate
            |    }
            |
            |    val kspExtension =
            |        extensions.findByName("ksp") as? KspExtension
            |            ?: return@afterEvaluate
            |    args.forEach { (key, value) ->
            |        kspExtension.arg(key, value)
            |    }
            |}
            |
            |pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            |    project.configureGeneratedJvmConsumer()
            |    val (groupId, version) = loadGeneratedCoordinates(generatedKspResourceClassLoader)
            |    dependencies.addGeneratedDependency(
            |        project = project,
            |        configurationName = processorConfigurationName(
            |            artifactKind = generatedProcessorArtifact.artifactKind,
            |            kotlinMultiplatform = false,
            |        ),
            |        localProjectPath = generatedProcessorArtifact.localProjectPath,
            |        artifactId = generatedProcessorArtifact.artifactId,
            |        notation = null,
            |        groupId = groupId,
            |        version = version,
            |    )
            |    generatedCompanionDependencies.forEach { dependency ->
            |        val configurationName =
            |            companionConfigurationName(
            |                scope = dependency.scope,
            |                artifactKind = dependency.artifactKind,
            |                kotlinMultiplatform = false,
            |            ) ?: return@forEach
            |        dependencies.addGeneratedDependency(
            |            project = project,
            |            configurationName = configurationName,
            |            localProjectPath = dependency.localProjectPath,
            |            artifactId = dependency.artifactId,
            |            notation = dependency.notation,
            |            groupId = groupId,
            |            version = version,
            |        )
            |    }
            |}
            |
            |pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            |    project.configureGeneratedKmpConsumer()
            |    val (groupId, version) = loadGeneratedCoordinates(generatedKspResourceClassLoader)
            |    dependencies.addGeneratedDependency(
            |        project = project,
            |        configurationName = processorConfigurationName(
            |            artifactKind = generatedProcessorArtifact.artifactKind,
            |            kotlinMultiplatform = true,
            |        ),
            |        localProjectPath = generatedProcessorArtifact.localProjectPath,
            |        artifactId = generatedProcessorArtifact.artifactId,
            |        notation = null,
            |        groupId = groupId,
            |        version = version,
            |    )
            |    generatedCompanionDependencies.forEach { dependency ->
            |        val configurationName =
            |            companionConfigurationName(
            |                scope = dependency.scope,
            |                artifactKind = dependency.artifactKind,
            |                kotlinMultiplatform = true,
            |            ) ?: return@forEach
            |        dependencies.addGeneratedDependency(
            |            project = project,
            |            configurationName = configurationName,
            |            localProjectPath = dependency.localProjectPath,
            |            artifactId = dependency.artifactId,
            |            notation = dependency.notation,
            |            groupId = groupId,
            |            version = version,
            |        )
            |    }
            |}
            |""".trimMargin()
    }

    private fun parseCompanionDependency(rawValue: String): ConsumerKspCompanionDependencySpec {
        val parts = rawValue.split("|")
        require(parts.size in 4..5) {
            "consumerKspBuildLogicCompanionDependencies 必须是 scope|artifactKind|localProjectPath|artifactId[|notation] 形式。收到: $rawValue"
        }
        val notation = parts.getOrNull(4)?.trim().orEmpty().ifBlank { null }
        return ConsumerKspCompanionDependencySpec(
            scope = parts[0].trim().uppercase(),
            artifactKind = parts[1].trim().uppercase(),
            localProjectPath = parts[2].trim().ifBlank { null },
            artifactId = parts[3].trim().ifBlank { null },
            notation = notation,
        )
    }

    private fun detectCommonPrefixToStrip(optionKeys: Collection<String>): String {
        if (optionKeys.isEmpty() || optionKeys.any { key -> !key.contains('.') }) {
            return ""
        }
        var prefix = optionKeys.first()
        optionKeys.drop(1).forEach { key ->
            while (!key.startsWith(prefix) && prefix.isNotEmpty()) {
                prefix = prefix.dropLast(1)
            }
        }
        val boundaryIndex = prefix.lastIndexOf('.')
        if (boundaryIndex < 0) {
            return ""
        }
        return prefix.substring(0, boundaryIndex + 1)
    }

    private fun derivePropertyName(
        optionKey: String,
        prefixToStrip: String,
        kotlinType: String,
    ): String {
        val normalizedKey = optionKey.removePrefix(prefixToStrip).replace(".default.", ".")
        var propertyName = normalizedKey.toLowCamelCase()
        if ((kotlinType.startsWith("List<") || kotlinType.startsWith("Set<")) && !propertyName.endsWith("s")) {
            propertyName += "s"
        }
        return propertyName
    }

    private fun shouldAlwaysEmit(
        defaultValue: String,
        kotlinType: String,
    ): Boolean {
        return when {
            kotlinType.startsWith("List<") -> {
                CodeGenHelper.toDefaultValueExpression(defaultValue, kotlinType) != "emptyList()"
            }

            kotlinType.startsWith("Set<") -> {
                CodeGenHelper.toDefaultValueExpression(defaultValue, kotlinType) != "emptySet()"
            }

            else -> {
                defaultValue.isNotBlank()
            }
        }
    }

    private fun gradlePropertyType(kotlinType: String): String {
        return when {
            kotlinType.startsWith("List<") -> {
                "ListProperty<${kotlinType.removePrefix("List<").removeSuffix(">")}>"
            }

            kotlinType.startsWith("Set<") -> {
                "SetProperty<${kotlinType.removePrefix("Set<").removeSuffix(">")}>"
            }

            else -> {
                "Property<$kotlinType>"
            }
        }
    }

    private fun renderArgStatement(property: ConsumerKspPropertySpec): String {
        return when {
            property.kotlinType.startsWith("List<") || property.kotlinType.startsWith("Set<") -> {
                if (property.alwaysEmit) {
                    """        put("${property.optionKey}", extension.${property.propertyName}.get().joinToString(","))"""
                } else {
                    """
                    |        extension.${property.propertyName}.get()
                    |            .takeIf { it.isNotEmpty() }
                    |            ?.let { put("${property.optionKey}", it.joinToString(",")) }""".trimMargin()
                }
            }

            property.kotlinType == "String" -> {
                if (property.alwaysEmit) {
                    """        put("${property.optionKey}", extension.${property.propertyName}.get())"""
                } else {
                    """
                    |        extension.${property.propertyName}.orNull
                    |            ?.takeIf(String::isNotBlank)
                    |            ?.let { put("${property.optionKey}", it) }""".trimMargin()
                }
            }

            else -> {
                if (property.alwaysEmit) {
                    """        put("${property.optionKey}", extension.${property.propertyName}.get().toString())"""
                } else {
                    """
                    |        extension.${property.propertyName}.orNull
                    |            ?.let { put("${property.optionKey}", it.toString()) }""".trimMargin()
                }
            }
        }
    }
}
