// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The aggregated completions usage details of the specific time bucket.
 */
@Serializable
data class UsageCompletionsResult(
    @SerialName("object")
    val objectType: String,
    /**
     * The aggregated number of text input tokens used, including cached tokens. For customers subscribe to
     * scale tier, this includes scale tier tokens.
     */
    @SerialName("input_tokens")
    val inputTokens: Int,
    /**
     * The aggregated number of text input tokens that has been cached from previous requests. For
     * customers subscribe to scale tier, this includes scale tier tokens.
     */
    @SerialName("input_cached_tokens")
    val inputCachedTokens: Int? = null,
    /**
     * The aggregated number of text output tokens used. For customers subscribe to scale tier, this
     * includes scale tier tokens.
     */
    @SerialName("output_tokens")
    val outputTokens: Int,
    /**
     * The aggregated number of audio input tokens used, including cached tokens.
     */
    @SerialName("input_audio_tokens")
    val inputAudioTokens: Int? = null,
    /**
     * The aggregated number of audio output tokens used.
     */
    @SerialName("output_audio_tokens")
    val outputAudioTokens: Int? = null,
    /**
     * The count of requests made to the model.
     */
    @SerialName("num_model_requests")
    val numModelRequests: Int,
    @SerialName("project_id")
    val projectId: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("api_key_id")
    val apiKeyId: String? = null,
    val model: String? = null,
    val batch: Boolean? = null,
    @SerialName("service_tier")
    val serviceTier: String? = null
)
