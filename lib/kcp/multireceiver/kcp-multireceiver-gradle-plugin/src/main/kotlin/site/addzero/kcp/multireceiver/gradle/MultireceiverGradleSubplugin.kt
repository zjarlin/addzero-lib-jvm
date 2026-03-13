package site.addzero.kcp.multireceiver.gradle

import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.util.Properties

class MultireceiverGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    private val logger = Logging.getLogger(MultireceiverGradleSubplugin::class.java)

    override fun apply(target: Project) {
        addAnnotationsDependency(target)
        enableContextParameters(target)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        if (kotlinCompilation.target.platformType != KotlinPlatformType.jvm) {
            return false
        }
        val project = kotlinCompilation.target.project
        if (shouldDisableCompilerPluginForIde(project)) {
            logger.info(
                "Disabling multireceiver compiler plugin for IDE sync/import in project ${project.path}",
            )
            return false
        }
        return true
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ) = kotlinCompilation.target.project.provider<List<SubpluginOption>> {
        emptyList()
    }

    override fun getCompilerPluginId(): String {
        return COMPILER_PLUGIN_ID
    }

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            coordinates.groupId,
            COMPILER_ARTIFACT_ID,
            coordinates.version,
        )
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getPluginArtifactForNative(): SubpluginArtifact {
        return getPluginArtifact()
    }

    private fun addAnnotationsDependency(project: Project) {
        if (project.extensions.extraProperties.has(ANNOTATIONS_MARKER)) {
            return
        }
        project.extensions.extraProperties.set(ANNOTATIONS_MARKER, true)
        val notation = "${coordinates.groupId}:$ANNOTATIONS_ARTIFACT_ID:${coordinates.version}"
        val configurationNames = listOf(
            "implementation",
            "api",
            "commonMainImplementation",
            "jvmMainImplementation",
        )
        configurationNames.forEach { configurationName ->
            if (project.configurations.findByName(configurationName) != null) {
                project.dependencies.add(configurationName, notation)
            }
        }
    }

    private fun enableContextParameters(project: Project) {
        project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            project.extensions.findByType(KotlinJvmProjectExtension::class.java)
                ?.compilerOptions
                ?.freeCompilerArgs
                ?.add(CONTEXT_PARAMETERS_FLAG)
        }
        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            project.extensions.findByType(KotlinMultiplatformExtension::class.java)
                ?.compilerOptions
                ?.freeCompilerArgs
                ?.add(CONTEXT_PARAMETERS_FLAG)
        }
    }

    private val coordinates: Coordinates by lazy {
        val properties = Properties()
        javaClass.classLoader
            .getResourceAsStream(PROPERTIES_RESOURCE)
            ?.use(properties::load)
            ?: error("Missing $PROPERTIES_RESOURCE")
        Coordinates(
            groupId = properties.getProperty("groupId"),
            version = properties.getProperty("version"),
        )
    }

    private data class Coordinates(
        val groupId: String,
        val version: String,
    )

    companion object {
        const val GRADLE_PLUGIN_ID: String = "site.addzero.kcp.multireceiver"
        const val COMPILER_PLUGIN_ID: String = "site.addzero.kcp.multireceiver"
        const val COMPILER_ARTIFACT_ID: String = "kcp-multireceiver-plugin"
        const val ANNOTATIONS_ARTIFACT_ID: String = "kcp-multireceiver-annotations"
        const val CONTEXT_PARAMETERS_FLAG: String = "-Xcontext-parameters"

        private const val ANNOTATIONS_MARKER = "site.addzero.kcp.multireceiver.annotations-added"
        private const val PROPERTIES_RESOURCE = "site/addzero/kcp/multireceiver/gradle-plugin.properties"
    }
}

internal fun shouldDisableCompilerPluginForIde(project: Project): Boolean {
    return shouldDisableCompilerPluginForIde(
        systemProperties = mapOf(
            "idea.active" to System.getProperty("idea.active"),
            "idea.sync.active" to System.getProperty("idea.sync.active"),
            "android.injected.invoked.from.ide" to System.getProperty("android.injected.invoked.from.ide"),
        ),
        taskNames = project.gradle.startParameter.taskNames,
    )
}

internal fun shouldDisableCompilerPluginForIde(
    systemProperties: Map<String, String?>,
    taskNames: Iterable<String>,
): Boolean {
    val isIdeaActive = systemProperties["idea.active"].equals("true", ignoreCase = true)
    val isIdeaSyncActive = systemProperties["idea.sync.active"].equals("true", ignoreCase = true)
    val isInvokedFromIde = systemProperties["android.injected.invoked.from.ide"].equals("true", ignoreCase = true)
    if (isIdeaActive || isIdeaSyncActive || isInvokedFromIde) {
        return true
    }
    return taskNames.any { taskName ->
        taskName == "ideaSyncTask" ||
            taskName == "prepareKotlinIdeaImport" ||
            taskName.endsWith("SyncTask")
    }
}
