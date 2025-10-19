package site.addzero.network.call.maven.util

import MavenVersionFetcher
import site.addzero.network.call.maven.entity.ArtifactDetails
import site.addzero.network.call.maven.internal.MavenArtifactTool

object MavenUtil {
     fun getArti(groupId: String, artifactId: String): ArtifactDetails? {
        val mavenArtifactTool = MavenArtifactTool()
        val artifactDetails = mavenArtifactTool.getArtifactDetails(groupId, artifactId)
        return artifactDetails
    }
    fun getLatestVersion(groupId: String, artifactId: String): String? {
        val fetcher = MavenVersionFetcher()
        val springBootVersion = fetcher.getLatestVersion(groupId, artifactId)
        return springBootVersion

    }
}
