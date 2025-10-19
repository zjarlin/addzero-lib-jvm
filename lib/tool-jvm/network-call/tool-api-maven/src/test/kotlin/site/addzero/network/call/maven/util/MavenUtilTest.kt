package site.addzero.network.call.maven.util

import com.fasterxml.jackson.core.util.VersionUtil
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MavenUtilTest {

    @Test
    fun `should get latest version of tool-api-maven`() {
        val version = MavenUtil.getLatestVersion("site.addzero", "")
        assertNotNull(version, "Version should not be null")
        assertTrue(version.isNotBlank(), "Version should not be blank")
    }

    @Test
    fun `should get artifact details of tool-api-maven`() {
        val details = MavenUtil.getArti("site.addzero", "")
        assertNotNull(details, "Details should not be null")
        assertEquals("site.addzero", details.groupId)
        assertEquals("tool-api-maven", details.artifactId)
        assertNotNull(details.latestVersion, "Latest version should not be null")
        assertNotNull(details.allVersions, "All versions should not be null")
        assertTrue(details.allVersions.isNotEmpty(), "Should have at least one version")
    }
}
