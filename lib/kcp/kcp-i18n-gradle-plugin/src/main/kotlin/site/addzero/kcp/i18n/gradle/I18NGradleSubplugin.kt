package site.addzero.kcp.i18n.gradle

import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.util.Properties

class I18NGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    private val logger = Logging.getLogger(I18NGradleSubplugin::class.java)

    override fun apply(target: Project) {
        target.extensions.create("i18n", I18NGradleExtension::class.java)
        addRuntimeDependency(target)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        if (kotlinCompilation.target.platformType != KotlinPlatformType.jvm) {
            return false
        }
        val project = kotlinCompilation.target.project
        if (shouldDisableCompilerPluginForIdeSync(project)) {
            logger.info(
                "Disabling i18n compiler plugin for IDE sync/import in project ${project.path}",
            )
            return false
        }
        return true
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ) = kotlinCompilation.target.project.provider<List<SubpluginOption>> {
        val extension = kotlinCompilation.target.project.extensions.getByType(I18NGradleExtension::class.java)
        listOf(
            SubpluginOption(TARGET_LOCALE_OPTION, extension.targetLocale.get()),
            SubpluginOption(RESOURCE_BASE_PATH_OPTION, extension.resourceBasePath.get()),
        )
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

    private fun addRuntimeDependency(project: Project) {
        if (project.extensions.extraProperties.has(RUNTIME_MARKER)) {
            return
        }
        project.extensions.extraProperties.set(RUNTIME_MARKER, true)
        val notation = "${coordinates.groupId}:$RUNTIME_ARTIFACT_ID:${coordinates.version}"
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
        const val GRADLE_PLUGIN_ID: String = "site.addzero.kcp.i18n"
        const val COMPILER_PLUGIN_ID: String = "site.addzero.kcp.i18n"
        const val COMPILER_ARTIFACT_ID: String = "kcp-i18n"
        const val RUNTIME_ARTIFACT_ID: String = "kcp-i18n-runtime"
        const val TARGET_LOCALE_OPTION: String = "targetLocale"
        const val RESOURCE_BASE_PATH_OPTION: String = "resourceBasePath"

        private const val RUNTIME_MARKER = "site.addzero.kcp.i18n.runtime-added"
        private const val PROPERTIES_RESOURCE = "site/addzero/kcp/i18n/gradle-plugin.properties"
    }
}

internal fun shouldDisableCompilerPluginForIdeSync(project: Project): Boolean {
    return shouldDisableCompilerPluginForIdeSync(
        systemProperties = emptyMap(),
        taskNames = project.gradle.startParameter.taskNames,
    )
}

internal fun shouldDisableCompilerPluginForIdeSync(
    @Suppress("UNUSED_PARAMETER") systemProperties: Map<String, String?>,
    taskNames: Iterable<String>,
): Boolean {
    return taskNames.any { taskName ->
        taskName == "ideaSyncTask" ||
            taskName == "prepareKotlinIdeaImport" ||
            taskName.endsWith("SyncTask")
    }
}
