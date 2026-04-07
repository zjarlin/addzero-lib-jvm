package site.addzero.ksp.gradle.test

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PublishedKspPluginMetadataTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("pluginSpecs")
    fun `published KSP plugins expose stable metadata and defaults`(spec: PublishedKspPluginSpec) {
        val marker = PropertiesLoader.load(pluginMarkerResource(spec.pluginId), javaClass.classLoader)
        assertTrue(marker.getValue("implementation-class").isNotBlank())

        val properties = PropertiesLoader.load(spec.resourcePath, javaClass.classLoader)
        assertEquals("site.addzero", properties["groupId"])
        assertTrue(properties.getValue("version").isNotBlank())
    }

    companion object {
        @JvmStatic
        fun pluginSpecs(): List<PublishedKspPluginSpec> = PublishedKspPluginSpecs.retained
    }
}

private fun pluginMarkerResource(pluginId: String): String =
    "META-INF/gradle-plugins/$pluginId.properties"

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
