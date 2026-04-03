package site.addzero.kcp.spreadpack

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.Properties

class SpreadPackGradlePluginCoordinatesTest {

    @Test
    fun exposes_generated_plugin_coordinates_resource() {
        val expectedGroup = System.getProperty("spreadPack.pluginGroup")
            ?: error("Missing spreadPack.pluginGroup system property")
        val expectedVersion = System.getProperty("spreadPack.pluginVersion")
            ?: error("Missing spreadPack.pluginVersion system property")
        val inputStream = SpreadPackGradleSubplugin::class.java.classLoader
            .getResourceAsStream("site/addzero/kcp/spreadpack/gradle-plugin.properties")

        assertNotNull(inputStream)

        val properties = Properties()
        inputStream!!.use(properties::load)

        assertEquals(expectedGroup, properties.getProperty("groupId"))
        assertEquals(expectedVersion, properties.getProperty("version"))
    }
}
