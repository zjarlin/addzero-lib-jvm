package site.addzero.kcp.allobjectjvmstatic.gradle

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.Properties

class AllObjectJvmStaticGradlePluginCoordinatesTest {

    @Test
    fun exposes_generated_plugin_coordinates_resource() {
        val inputStream = AllObjectJvmStaticGradleSubplugin::class.java.classLoader
            .getResourceAsStream("site/addzero/kcp/allobjectjvmstatic/gradle-plugin.properties")
        assertNotNull(inputStream)

        val properties = Properties()
        inputStream!!.use(properties::load)

        assertEquals(System.getProperty("allObjectJvmStatic.pluginGroup"), properties.getProperty("groupId"))
        assertEquals(System.getProperty("allObjectJvmStatic.pluginVersion"), properties.getProperty("version"))
    }
}
