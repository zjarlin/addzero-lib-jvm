// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a streamed chunk of a chat completion response returned by the model, based on the
 * provided input. [Learn more](/docs/guides/streaming-responses).
 */
@Serializable
data class CreateChatCompletionStreamResponse(
    /**
     * A unique identifier for the chat completion. Each chunk has the same ID.
     */
    val id: String,
    /**
     * A list of chat completion choices. Can contain more than one elements if `n` is greater than 1. Can
     * also be empty for the last chunk if you set `stream_options: {"include_usage": true}`.
     */
    val choices: List<site.addzero.api.openai.models.CreateChatCompletionStreamResponseChoice>,
    /**
     * The Unix timestamp (in seconds) of when the chat completion was created. Each chunk has the same
     * timestamp.
     */
    val created: Long,
    /**
     * The model to generate the completion.
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
     * The object type, which is always `chat.completion.chunk`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * An optional field that will only be present when you set `stream_options: {"include_usage": true}`
     * in your request. When present, it contains a null value **except for the last chunk** which contains
     * the token usage statistics for the entire request. **NOTE:** If the stream is interrupted or
     * cancelled, you may not receive the final usage chunk which contains the total token usage for the
     * request.
     */
    val usage: site.addzero.api.openai.models.CompletionUsage? = null
)
