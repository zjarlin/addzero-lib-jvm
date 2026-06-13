// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The payload used to update the rate limits.
 */
@Serializable
data class AuditLogRateLimitUpdatedChangesRequested(
    /**
     * The maximum requests per minute.
     */
    @SerialName("max_requests_per_1_minute")
    val maxRequestsPer1Minute: Int? = null,
    /**
     * The maximum tokens per minute.
     */
    @SerialName("max_tokens_per_1_minute")
    val maxTokensPer1Minute: Int? = null,
    /**
     * The maximum images per minute. Only relevant for certain models.
     */
    @SerialName("max_images_per_1_minute")
    val maxImagesPer1Minute: Int? = null,
    /**
     * The maximum audio megabytes per minute. Only relevant for certain models.
     */
    @SerialName("max_audio_megabytes_per_1_minute")
    val maxAudioMegabytesPer1Minute: Int? = null,
    /**
     * The maximum requests per day. Only relevant for certain models.
     */
    @SerialName("max_requests_per_1_day")
    val maxRequestsPer1Day: Int? = null,
    /**
     * The maximum batch input tokens per day. Only relevant for certain models.
     */
    @SerialName("batch_1_day_max_input_tokens")
    val batch1DayMaxInputTokens: Int? = null
)
