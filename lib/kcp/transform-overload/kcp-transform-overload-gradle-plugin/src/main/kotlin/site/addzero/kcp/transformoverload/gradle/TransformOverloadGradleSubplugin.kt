package site.addzero.kcp.transformoverload.gradle

import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.util.Properties

class TransformOverloadGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    private val logger = Logging.getLogger(TransformOverloadGradleSubplugin::class.java)

    override fun apply(target: Project) {
        addAnnotationsDependency(target)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        if (kotlinCompilation.target.platformType != KotlinPlatformType.jvm) {
            return false
        }
        val project = kotlinCompilation.target.project
        if (shouldDisableCompilerPluginForIdeSync(project)) {
            logger.info(
                "Disabling transform-overload compiler plugin for IDE sync/import in project ${project.path}",
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
        const val GRADLE_PLUGIN_ID: String = "site.addzero.kcp.transform-overload"
        const val COMPILER_PLUGIN_ID: String = "site.addzero.kcp.transform-overload"
        const val COMPILER_ARTIFACT_ID: String = "kcp-transform-overload-plugin"
        const val ANNOTATIONS_ARTIFACT_ID: String = "kcp-transform-overload-annotations"

        private const val ANNOTATIONS_MARKER = "site.addzero.kcp.transform-overload.annotations-added"
        private const val PROPERTIES_RESOURCE =
            "site/addzero/kcp/transformoverload/gradle-plugin.properties"
    }
}

internal fun shouldDisableCompilerPluginForIdeSync(project: Project): Boolean {
    return shouldDisableCompilerPluginForIdeSync(
        systemProperties = mapOf(
            "idea.active" to System.getProperty("idea.active"),
            "idea.sync.active" to System.getProperty("idea.sync.active"),
            "android.injected.invoked.from.ide" to System.getProperty("android.injected.invoked.from.ide"),
        ),
        taskNames = project.gradle.startParameter.taskNames,
    )
}

internal fun shouldDisableCompilerPluginForIdeSync(
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
