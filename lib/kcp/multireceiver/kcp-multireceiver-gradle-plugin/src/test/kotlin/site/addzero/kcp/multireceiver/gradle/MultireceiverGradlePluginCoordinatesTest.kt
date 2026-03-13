package site.addzero.kcp.multireceiver.gradle

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.Properties

class MultireceiverGradlePluginCoordinatesTest {

    @Test
    fun exposes_published_coordinates_to_subplugin_runtime() {
        val expectedGroup = System.getProperty("multireceiver.pluginGroup")
            ?: error("Missing multireceiver.pluginGroup system property")
        val expectedVersion = System.getProperty("multireceiver.pluginVersion")
            ?: error("Missing multireceiver.pluginVersion system property")
        val resourcePath = "site/addzero/kcp/multireceiver/gradle-plugin.properties"
        val inputStream = MultireceiverGradleSubplugin::class.java.classLoader
            .getResourceAsStream(resourcePath)

        assertNotNull(inputStream, "Missing $resourcePath")

        val properties = Properties()
        inputStream!!.use(properties::load)

        assertEquals(expectedGroup, properties.getProperty("groupId"))
        assertEquals(expectedVersion, properties.getProperty("version"))
    }
}
