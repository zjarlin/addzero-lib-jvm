package site.addzero.gradle.kspconsumer

import com.google.devtools.ksp.gradle.KspAATask
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.util.Properties

enum class PublishedKspArtifactKind {
    JVM,
    KMP,
}

enum class PublishedDependencyScope {
    IMPLEMENTATION,
    API,
    COMPILE_ONLY,
}

data class PublishedCompanionDependency(
    val scope: PublishedDependencyScope,
    val artifactKind: PublishedKspArtifactKind,
    val localProjectPath: String? = null,
    val artifactId: String? = null,
    val notation: String? = null,
)

data class PublishedProcessorArtifact(
    val artifactKind: PublishedKspArtifactKind,
    val localProjectPath: String? = null,
    val artifactId: String,
)

data class PublishedKspCoordinates(
    val groupId: String,
    val version: String,
)

data class PublishedKspConsumerDefinition(
    val pluginId: String,
    val coordinatesResourcePath: String,
    val resourceClassLoader: ClassLoader,
    val processorArtifact: PublishedProcessorArtifact,
    val companionDependencies: List<PublishedCompanionDependency> = emptyList(),
    val additionalProcessorArtifacts: List<PublishedProcessorArtifact> = emptyList(),
)

fun Project.configurePublishedKspConsumer(
    definition: PublishedKspConsumerDefinition,
    collectKspArgs: Project.() -> Map<String, String> = { emptyMap() },
) {
    pluginManager.apply("com.google.devtools.ksp")

    configurePublishedKspArguments(
        pluginId = definition.pluginId,
        collectKspArgs = collectKspArgs,
    )

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configureJvmPublishedKspConsumer()
        addPublishedProcessorArtifacts(
            definition = definition,
            kotlinMultiplatform = false,
        )
        addPublishedCompanionDependencies(
            definition = definition,
            kotlinMultiplatform = false,
        )
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        configureKmpPublishedKspConsumer()
        addPublishedProcessorArtifacts(
            definition = definition,
            kotlinMultiplatform = true,
        )
        addPublishedCompanionDependencies(
            definition = definition,
            kotlinMultiplatform = true,
        )
    }
}

fun loadPublishedKspCoordinates(
    resourcePath: String,
    classLoader: ClassLoader,
): PublishedKspCoordinates {
    val resource = classLoader.getResourceAsStream(resourcePath)
        ?: error("缺少坐标资源: $resourcePath")
    val properties = Properties()
    resource.use(properties::load)
    return PublishedKspCoordinates(
        groupId = properties.getProperty("groupId")
            ?: error("缺少 groupId: $resourcePath"),
        version = properties.getProperty("version")
            ?: error("缺少 version: $resourcePath"),
    )
}

fun Project.defaultPublishedKspSourceDirectory(): String {
    extensions.findByType(KotlinMultiplatformExtension::class.java)
        ?.sourceSets
        ?.findByName("commonMain")
        ?.kotlin
        ?.srcDirs
        ?.firstOrNull()
        ?.let(File::getAbsolutePath)
        ?.let { return it }

    extensions.findByType(KotlinJvmProjectExtension::class.java)
        ?.sourceSets
        ?.findByName("main")
        ?.kotlin
        ?.srcDirs
        ?.firstOrNull()
        ?.let(File::getAbsolutePath)
        ?.let { return it }

    extensions.findByType(SourceSetContainer::class.java)
        ?.findByName("main")
        ?.let { sourceSet ->
            sourceSet.extensions.findByName("kotlin")
                ?.let { it as? SourceDirectorySet }
                ?.srcDirs
                ?.firstOrNull()
                ?.absolutePath
                ?.let { return it }
            sourceSet.java.srcDirs.firstOrNull()?.absolutePath?.let { return it }
        }

    return layout.projectDirectory.dir("src/main/kotlin").asFile.absolutePath
}

fun publishedKspPackageDirectory(
    baseDir: String,
    packageName: String,
): String {
    return File(baseDir, packageName.replace(".", "/")).absolutePath
}

fun publishedKspSerializedArgsPropertyName(pluginId: String): String =
    "site.addzero.kspconsumer.$pluginId.serializedArgs"

@Deprecated(
    message = "Use configurePublishedKspConsumer(...) from a precompiled script plugin entrypoint.",
    replaceWith = ReplaceWith("project.configurePublishedKspConsumer(...)"),
)
abstract class AbstractPublishedKspConsumerPlugin : Plugin<Project> {

    protected abstract val pluginId: String
    protected abstract val coordinatesResourcePath: String
    protected abstract val processorArtifact: PublishedProcessorArtifact

    protected open val companionDependencies: List<PublishedCompanionDependency> = emptyList()
    protected open val additionalProcessorArtifacts: List<PublishedProcessorArtifact> = emptyList()

    protected open fun createExtension(project: Project): Any? = null

    protected open fun collectKspArgs(project: Project, extension: Any?): Map<String, String> = emptyMap()

    override fun apply(project: Project) {
        val extension = createExtension(project)
        project.configurePublishedKspConsumer(
            definition = PublishedKspConsumerDefinition(
                pluginId = pluginId,
                coordinatesResourcePath = coordinatesResourcePath,
                resourceClassLoader = javaClass.classLoader,
                processorArtifact = processorArtifact,
                companionDependencies = companionDependencies,
                additionalProcessorArtifacts = additionalProcessorArtifacts,
            ),
        ) {
            collectKspArgs(this, extension)
        }
    }

    protected fun <T : Any> createTypedExtension(
        project: Project,
        name: String,
        type: Class<T>,
        vararg constructionArguments: Any,
    ): T {
        return project.extensions.create(name, type, *constructionArguments)
    }

    protected fun loadPublishedCoordinates(): PublishedKspCoordinates {
        return loadPublishedKspCoordinates(
            resourcePath = coordinatesResourcePath,
            classLoader = javaClass.classLoader,
        )
    }

    protected fun defaultSourceDirectory(project: Project): String {
        return project.defaultPublishedKspSourceDirectory()
    }

    protected fun packageDirectory(
        baseDir: String,
        packageName: String,
    ): String {
        return publishedKspPackageDirectory(baseDir, packageName)
    }

    companion object {
        fun serializedArgsPropertyName(pluginId: String): String =
            publishedKspSerializedArgsPropertyName(pluginId)
    }
}

private fun Project.configurePublishedKspArguments(
    pluginId: String,
    collectKspArgs: Project.() -> Map<String, String>,
) {
    afterEvaluate {
        val args = collectKspArgs(this)
        extensions.extraProperties.set(publishedKspSerializedArgsPropertyName(pluginId), args)
        if (args.isEmpty()) {
            return@afterEvaluate
        }

        val kspExtension = extensions.findByName("ksp") as? KspExtension
            ?: return@afterEvaluate
        args.forEach { (key, value) ->
            kspExtension.arg(key, value)
        }
    }
}

private fun Project.addPublishedProcessorArtifacts(
    definition: PublishedKspConsumerDefinition,
    kotlinMultiplatform: Boolean,
) {
    val coordinates = loadPublishedKspCoordinates(
        resourcePath = definition.coordinatesResourcePath,
        classLoader = definition.resourceClassLoader,
    )
    val targetConfiguration = processorConfigurationName(
        artifactKind = definition.processorArtifact.artifactKind,
        kotlinMultiplatform = kotlinMultiplatform,
    )
    dependencies.addPublishedArtifact(
        project = this,
        configurationName = targetConfiguration,
        artifact = definition.processorArtifact,
        coordinates = coordinates,
    )
    definition.additionalProcessorArtifacts.forEach { artifact ->
        val configurationName = processorConfigurationName(
            artifactKind = artifact.artifactKind,
            kotlinMultiplatform = kotlinMultiplatform,
        )
        dependencies.addPublishedArtifact(
            project = this,
            configurationName = configurationName,
            artifact = artifact,
            coordinates = coordinates,
        )
    }
}

private fun Project.addPublishedCompanionDependencies(
    definition: PublishedKspConsumerDefinition,
    kotlinMultiplatform: Boolean,
) {
    val coordinates = loadPublishedKspCoordinates(
        resourcePath = definition.coordinatesResourcePath,
        classLoader = definition.resourceClassLoader,
    )
    definition.companionDependencies.forEach { dependency ->
        val configurationName = companionConfigurationName(
            scope = dependency.scope,
            artifactKind = dependency.artifactKind,
            kotlinMultiplatform = kotlinMultiplatform,
        )
        if (configurationName == null) {
            return@forEach
        }
        dependencies.addPublishedDependency(
            project = this,
            configurationName = configurationName,
            localProjectPath = dependency.localProjectPath,
            artifactId = dependency.artifactId,
            notation = dependency.notation,
            coordinates = coordinates,
        )
    }
}

private fun Project.configureJvmPublishedKspConsumer() {
    extensions.findByType(KotlinJvmProjectExtension::class.java)
        ?.sourceSets
        ?.findByName("main")
        ?.kotlin
        ?.srcDir(JVM_KSP_GENERATED_DIR)

    tasks.withType(KotlinCompile::class.java).configureEach {
        if (name != "kspKotlin") {
            dependsOn("kspKotlin")
        }
    }

    tasks.configureEach {
        if (name == "sourcesJar") {
            dependsOn("kspKotlin")
        }
    }
}

private fun Project.configureKmpPublishedKspConsumer() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.let { kotlin ->
        kotlin.sourceSets.findByName("commonMain")?.kotlin?.srcDir(KMP_COMMON_KSP_GENERATED_DIR)
        kotlin.sourceSets.findByName("jvmMain")?.kotlin?.srcDir(KMP_JVM_KSP_GENERATED_DIR)
    }

    val commonMainTaskName = "kspCommonMainKotlinMetadata"
    fun Task.dependsOnCommonMainIfPresent() {
        if (name == commonMainTaskName) {
            return
        }
        if (tasks.names.contains(commonMainTaskName)) {
            dependsOn(commonMainTaskName)
        }
    }

    tasks.withType(KotlinCompile::class.java).configureEach {
        dependsOnCommonMainIfPresent()
        if (name == "compileKotlinJvm" && tasks.names.contains("kspKotlinJvm")) {
            dependsOn("kspKotlinJvm")
        }
    }
    tasks.withType(Kotlin2JsCompile::class.java).configureEach {
        dependsOnCommonMainIfPresent()
    }
    tasks.withType(KspAATask::class.java).configureEach {
        dependsOnCommonMainIfPresent()
    }
    tasks.configureEach {
        if (name.contains("sourcesJar") || name == "jvmJar") {
            dependsOnCommonMainIfPresent()
        }
        if (name in setOf("jvmJar", "jvmSourcesJar") && tasks.names.contains("kspKotlinJvm")) {
            dependsOn("kspKotlinJvm")
        }
    }
}

private fun processorConfigurationName(
    artifactKind: PublishedKspArtifactKind,
    kotlinMultiplatform: Boolean,
): String {
    return if (!kotlinMultiplatform) {
        "ksp"
    } else {
        when (artifactKind) {
            PublishedKspArtifactKind.KMP -> "kspCommonMainMetadata"
            PublishedKspArtifactKind.JVM -> "kspJvm"
        }
    }
}

private fun companionConfigurationName(
    scope: PublishedDependencyScope,
    artifactKind: PublishedKspArtifactKind,
    kotlinMultiplatform: Boolean,
): String? {
    val prefix = if (!kotlinMultiplatform) {
        ""
    } else {
        when (artifactKind) {
            PublishedKspArtifactKind.KMP -> "commonMain"
            PublishedKspArtifactKind.JVM -> "jvmMain"
        }
    }
    val suffix = when (scope) {
        PublishedDependencyScope.IMPLEMENTATION -> "Implementation"
        PublishedDependencyScope.API -> "Api"
        PublishedDependencyScope.COMPILE_ONLY -> "CompileOnly"
    }
    return if (prefix.isBlank()) {
        suffix.replaceFirstChar(Char::lowercase)
    } else {
        prefix + suffix
    }
}

private const val JVM_KSP_GENERATED_DIR = "build/generated/ksp/main/kotlin"
private const val KMP_COMMON_KSP_GENERATED_DIR = "build/generated/ksp/metadata/commonMain/kotlin"
private const val KMP_JVM_KSP_GENERATED_DIR = "build/generated/ksp/jvm/jvmMain/kotlin"

private fun DependencyHandler.addPublishedArtifact(
    project: Project,
    configurationName: String,
    artifact: PublishedProcessorArtifact,
    coordinates: PublishedKspCoordinates,
) {
    addPublishedDependency(
        project = project,
        configurationName = configurationName,
        localProjectPath = artifact.localProjectPath,
        artifactId = artifact.artifactId,
        notation = null,
        coordinates = coordinates,
    )
}

private fun DependencyHandler.addPublishedDependency(
    project: Project,
    configurationName: String,
    localProjectPath: String?,
    artifactId: String?,
    notation: String?,
    coordinates: PublishedKspCoordinates,
) {
    if (project.configurations.findByName(configurationName) == null) {
        return
    }

    when {
        localProjectPath != null && project.rootProject.findProject(localProjectPath) != null -> {
            add(configurationName, project.project(localProjectPath))
        }

        notation != null -> {
            add(configurationName, notation)
        }

        artifactId != null -> {
            add(configurationName, "${coordinates.groupId}:$artifactId:${coordinates.version}")
        }

        else -> {
            error("依赖配置缺少 artifactId/notion/localProjectPath")
        }
    }
}
