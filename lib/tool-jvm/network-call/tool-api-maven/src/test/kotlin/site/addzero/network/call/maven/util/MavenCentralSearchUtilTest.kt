package site.addzero.network.call.maven.util

import org.junit.jupiter.api.Test

private const val IO_GITHUB_ZEPHYRCICD = "io.github.zephyrcicd"

class MavenCentralSearchUtilTest {

    @Test
    fun `should search artifacts by group id with new API`() {
        val artifacts = MavenCentralSearchUtil.searchByGroupId(IO_GITHUB_ZEPHYRCICD, 5)
        println()

    }

    @Test
    fun `should get latest version by group id with new API`() {
//        val latestVersion = MavenCentralSearchUtil.getLatestVersionByGroupId("org.babyfish.jimmer")
//        val latestVersion1 = MavenCentralSearchUtil.getLatestVersionByGroupId("site.addzero")
        val latestVersion12 = MavenCentralSearchUtil .getLatestVersionByGroupId(IO_GITHUB_ZEPHYRCICD)
//        assertNotNull(latestVersion, "Should get latest version for group id")
//        println("Latest version in group 'org.babyfish.jimmer' is $latestVersion")
        println()
    }

    @Test
    fun `should search artifacts for jackson group with new API`() {
        val artifacts = MavenCentralSearchUtil.searchByGroupId("site.addzero", 5)
        println()
    }
    @Test
    fun `search by ati`(): Unit {
//        val searchByArtifactId = MavenCentralSearchUtil.searchByArtifactId("tool-api-maven", 5)
//        val searchByArtifactId1= MavenCentralSearchUtil.searchByArtifactId ("mybatis-auto-wrapper", 5)
//        val searchByArtifactId1= MavenCentralSearchUtil.searchByArtifactId ("tool-database-model", 5)


        val searchByArtifactId2= MavenCentralSearchUtil.searchByArtifactId ("tool-coll", 5)
        println()

    }
}
