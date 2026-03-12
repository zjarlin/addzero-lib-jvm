package site.addzero.kcp.transformoverload.gradle

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.Properties

class TransformOverloadGradlePluginCoordinatesTest {

    @Test
    fun exposes_published_coordinates_to_subplugin_runtime() {
        val expectedGroup = System.getProperty("transformOverload.pluginGroup")
            ?: error("Missing transformOverload.pluginGroup system property")
        val expectedVersion = System.getProperty("transformOverload.pluginVersion")
            ?: error("Missing transformOverload.pluginVersion system property")
        val resourcePath = "site/addzero/kcp/transformoverload/gradle-plugin.properties"
        val inputStream = TransformOverloadGradleSubplugin::class.java.classLoader
            .getResourceAsStream(resourcePath)

        assertNotNull(inputStream, "Missing $resourcePath")

        val properties = Properties()
        inputStream!!.use(properties::load)

        assertEquals(expectedGroup, properties.getProperty("groupId"))
        assertEquals(expectedVersion, properties.getProperty("version"))
    }
}
