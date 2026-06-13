// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Structured information describing a generated video job.
 */
@Serializable
data class VideoResource(
    /**
     * Unique identifier for the video job.
     */
    val id: String,
    /**
     * The object type, which is always `video`.
     */
    @SerialName("object")
    val objectType: String = "video",
    /**
     * The video generation model that produced the job.
     */
    val model: site.addzero.api.openai.models.VideoModel,
    /**
     * Current lifecycle status of the video job.
     */
    val status: site.addzero.api.openai.models.VideoStatus,
    /**
     * Approximate completion percentage for the generation task.
     */
    val progress: Int,
    /**
     * Unix timestamp (seconds) for when the job was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("completed_at")
    val completedAt: Long?,
    @SerialName("expires_at")
    val expiresAt: Long?,
    val prompt: String?,
    /**
     * The resolution of the generated video.
     */
    val size: site.addzero.api.openai.models.VideoSize,
    /**
     * Duration of the generated clip in seconds. For extensions, this is the stitched total duration.
     */
    val seconds: String,
    @SerialName("remixed_from_video_id")
    val remixedFromVideoId: String?,
    val error: site.addzero.api.openai.models.Error2?
)
