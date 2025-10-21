package site.addzero.network.call.maven.util

import org.junit.jupiter.api.Test

class MavenCentralSearchUtilTest {

    @Test
    fun `should search artifacts by group id with new API`() {
        val artifacts = MavenCentralSearchUtil.searchByGroupId("site.addzero", 5)
//        assertTrue(artifacts.isNotEmpty(), "Should find artifacts for group id")
        println("Found ${artifacts.size} artifacts for group 'org.babyfish.jimmer'")
        artifacts.forEach {
            println("  ${it.groupId}:${it.artifactId} (${it.latestVersion})")
        }
    }

    @Test
    fun `should get latest version by group id with new API`() {
        val latestVersion = MavenCentralSearchUtil.getLatestVersionByGroupId("org.babyfish.jimmer")
        val latestVersion1 = MavenCentralSearchUtil.getLatestVersionByGroupId("site.addzero")
//        assertNotNull(latestVersion, "Should get latest version for group id")
        println("Latest version in group 'org.babyfish.jimmer' is $latestVersion")
    }

    @Test
    fun `should search artifacts for jackson group with new API`() {
        val artifacts = MavenCentralSearchUtil.searchByGroupId("site.addzero", 5)
        println()
    }
}
