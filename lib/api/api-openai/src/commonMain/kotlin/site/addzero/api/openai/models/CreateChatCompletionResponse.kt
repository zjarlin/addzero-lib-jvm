// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a chat completion response returned by model, based on the provided input.
 */
@Serializable
data class CreateChatCompletionResponse(
    /**
     * A unique identifier for the chat completion.
     */
    val id: String,
    /**
     * A list of chat completion choices. Can be more than one if `n` is greater than 1.
     */
    val choices: List<site.addzero.api.openai.models.CreateChatCompletionResponseChoice>,
    /**
     * The Unix timestamp (in seconds) of when the chat completion was created.
     */
    val created: Long,
    /**
     * The model used for the chat completion.
     */
    val model: String,
    @SerialName("service_tier")
    val serviceTier: site.addzero.api.openai.models.ServiceTier? = null,
    /**
     * This fingerprint represents the backend configuration that the model runs with. Can be used in
     * conjunction with the `seed` request parameter to understand when backend changes have been made that
     * might impact determinism.
     */
    @SerialName("system_fingerprint")
    val systemFingerprint: String? = null,
    /**
     * The object type, which is always `chat.completion`.
     */
    @SerialName("object")
    val objectType: String,
    val usage: site.addzero.api.openai.models.CompletionUsage? = null
)
