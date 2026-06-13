// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a completion response from the API. Note: both the streamed and non-streamed response
 * objects share the same shape (unlike the chat endpoint).
 */
@Serializable
data class CreateCompletionResponse(
    /**
     * A unique identifier for the completion.
     */
    val id: String,
    /**
     * The list of completion choices the model generated for the input prompt.
     */
    val choices: List<site.addzero.api.openai.models.CreateCompletionResponseChoice>,
    /**
     * The Unix timestamp (in seconds) of when the completion was created.
     */
    val created: Long,
    /**
     * The model used for completion.
     */
    val model: String,
    /**
     * This fingerprint represents the backend configuration that the model runs with. Can be used in
     * conjunction with the `seed` request parameter to understand when backend changes have been made that
     * might impact determinism.
     */
    @SerialName("system_fingerprint")
    val systemFingerprint: String? = null,
    /**
     * The object type, which is always "text_completion"
     */
    @SerialName("object")
    val objectType: String,
    val usage: site.addzero.api.openai.models.CompletionUsage? = null
)
