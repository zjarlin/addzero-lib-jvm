// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a project rate limit config.
 */
@Serializable
data class ProjectRateLimit(
    /**
     * The object type, which is always `project.rate_limit`
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The model this rate limit applies to.
     */
    val model: String,
    /**
     * The maximum requests per minute.
     */
    @SerialName("max_requests_per_1_minute")
    val maxRequestsPer1Minute: Int,
    /**
     * The maximum tokens per minute.
     */
    @SerialName("max_tokens_per_1_minute")
    val maxTokensPer1Minute: Int,
    /**
     * The maximum images per minute. Only present for relevant models.
     */
    @SerialName("max_images_per_1_minute")
    val maxImagesPer1Minute: Int? = null,
    /**
     * The maximum audio megabytes per minute. Only present for relevant models.
     */
    @SerialName("max_audio_megabytes_per_1_minute")
    val maxAudioMegabytesPer1Minute: Int? = null,
    /**
     * The maximum requests per day. Only present for relevant models.
     */
    @SerialName("max_requests_per_1_day")
    val maxRequestsPer1Day: Int? = null,
    /**
     * The maximum batch input tokens per day. Only present for relevant models.
     */
    @SerialName("batch_1_day_max_input_tokens")
    val batch1DayMaxInputTokens: Int? = null
)
