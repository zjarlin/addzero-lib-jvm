// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details about the input tokens billed for this request.
 */
@Serializable
data class TranscriptTextUsageTokensInputTokenDetails(
    /**
     * Number of text tokens billed for this request.
     */
    @SerialName("text_tokens")
    val textTokens: Int? = null,
    /**
     * Number of audio tokens billed for this request.
     */
    @SerialName("audio_tokens")
    val audioTokens: Int? = null
)
