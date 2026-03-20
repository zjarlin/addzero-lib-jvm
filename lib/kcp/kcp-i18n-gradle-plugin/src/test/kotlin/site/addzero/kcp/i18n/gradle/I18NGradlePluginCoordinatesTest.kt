package site.addzero.kcp.i18n.gradle

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.Properties

class I18NGradlePluginCoordinatesTest {

    @Test
    fun exposes_published_coordinates_to_subplugin_runtime() {
        val expectedGroup = System.getProperty("i18n.pluginGroup")
            ?: error("Missing i18n.pluginGroup system property")
        val expectedVersion = System.getProperty("i18n.pluginVersion")
            ?: error("Missing i18n.pluginVersion system property")
        val resourcePath = "site/addzero/kcp/i18n/gradle-plugin.properties"
        val inputStream = I18NGradleSubplugin::class.java.classLoader
            .getResourceAsStream(resourcePath)

        assertNotNull(inputStream, "Missing $resourcePath")

        val properties = Properties()
        inputStream!!.use(properties::load)

        assertEquals(expectedGroup, properties.getProperty("groupId"))
        assertEquals(expectedVersion, properties.getProperty("version"))
    }
}
