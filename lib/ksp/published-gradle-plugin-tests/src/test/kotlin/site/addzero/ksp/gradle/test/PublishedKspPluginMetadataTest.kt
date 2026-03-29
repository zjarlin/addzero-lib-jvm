package site.addzero.ksp.gradle.test

import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PublishedKspPluginMetadataTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("pluginSpecs")
    fun `published KSP plugins expose stable metadata and defaults`(spec: PublishedKspPluginSpec) {
        val implementationClass = loadPluginClass(spec)

        assertEquals(spec.pluginId, staticString(implementationClass, "PLUGIN_ID"))
        assertEquals(spec.processorArtifactId, staticString(implementationClass, "PROCESSOR_ARTIFACT_ID"))
        assertEquals(spec.resourcePath, staticString(implementationClass, "COORDINATES_RESOURCE_PATH"))

        val properties = PropertiesLoader.load(spec.resourcePath, implementationClass.classLoader)
        assertEquals("site.addzero", properties["groupId"])
        assertTrue(properties.getValue("version").isNotBlank())

        if (spec.extensionName != null && spec.verifyExtensionDefaults) {
            val extension = createExtension(spec, implementationClass)
            spec.defaultValues.forEach { (path, expected) ->
                assertEquals(expected, readValue(extension, path), "${spec.pluginId} default for $path")
            }
        }
    }

    private fun createExtension(
        spec: PublishedKspPluginSpec,
        implementationClass: Class<out Plugin<Project>>,
    ): Any {
        val projectDir = Files.createTempDirectory(spec.pluginId.substringAfterLast('.'))
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir.toFile())
            .build()
        val constructor = implementationClass.getDeclaredConstructor().apply { isAccessible = true }
        val plugin = constructor.newInstance()
        val createExtension = implementationClass
            .getDeclaredMethod("createExtension", Project::class.java)
            .apply { isAccessible = true }
        return createExtension.invoke(plugin, project)
    }

    private fun readValue(target: Any, path: String): Any? {
        var current: Any? = target
        for (segment in path.split('.')) {
            val getterName = "get" + segment.replaceFirstChar(Char::uppercase)
            current = current?.javaClass?.methods
                ?.firstOrNull { method -> method.name == getterName && method.parameterCount == 0 }
                ?.invoke(current)
        }
        return when (current) {
            is Property<*> -> current.orNull
            is ListProperty<*> -> current.get()
            is Provider<*> -> current.orNull
            else -> current
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadPluginClass(spec: PublishedKspPluginSpec): Class<out Plugin<Project>> {
        return Class.forName(spec.implementationClass) as Class<out Plugin<Project>>
    }

    private fun staticString(clazz: Class<*>, fieldName: String): String {
        val field = clazz.getDeclaredField(fieldName)
        return field.get(null) as String
    }

    companion object {
        @JvmStatic
        fun pluginSpecs(): List<PublishedKspPluginSpec> = PublishedKspPluginSpecs.all
    }
}

private object PropertiesLoader {
    fun load(resourcePath: String, classLoader: ClassLoader): Map<String, String> {
        val input = classLoader.getResourceAsStream(resourcePath)
        assertNotNull(input, "Missing coordinates resource $resourcePath")
        val properties = java.util.Properties()
        input.use(properties::load)
        return properties.entries.associate { (key, value) ->
            key.toString() to value.toString()
        }
    }
}
