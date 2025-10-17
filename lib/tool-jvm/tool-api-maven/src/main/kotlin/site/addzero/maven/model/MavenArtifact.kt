package site.addzero.maven.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Maven构件信息
 *
 * @author zjarlin
 * @since 2025/10/15
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MavenArtifact(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("g")
    val groupId: String,
    @JsonProperty("a")
    val artifactId: String,
    @JsonProperty("latestVersion")
    val latestVersion: String?,
    @JsonProperty("versionCount")
    val versionCount: Int = 0,
    @JsonProperty("timestamp")
    val timestamp: Long? = null
) {
    /**
     * 获取构件的坐标格式字符串
     */
    fun getCoordinate(): String = "$groupId:$artifactId"

    /**
     * 获取带版本的坐标格式字符串
     */
    fun getCoordinateWithVersion(): String = "$groupId:$artifactId:$latestVersion"
}

/**
 * Maven搜索响应
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MavenSearchResponse(
    @JsonProperty("response")
    val response: ResponseData
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ResponseData(
        @JsonProperty("numFound")
        val numFound: Int,
        @JsonProperty("start")
        val start: Int,
        @JsonProperty("docs")
        val docs: List<MavenArtifact>
    )

    /**
     * 获取构件列表
     */
    fun getArtifacts(): List<MavenArtifact> = response.docs

    /**
     * 获取总数量
     */
    fun getTotalCount(): Int = response.numFound
}