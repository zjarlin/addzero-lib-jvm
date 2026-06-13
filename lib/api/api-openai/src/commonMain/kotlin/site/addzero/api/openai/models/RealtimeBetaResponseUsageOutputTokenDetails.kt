// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details about the output tokens used in the Response.
 */
@Serializable
data class RealtimeBetaResponseUsageOutputTokenDetails(
    /**
     * The number of text tokens used in the Response.
     */
    @SerialName("text_tokens")
    val textTokens: Int? = null,
    /**
     * The number of audio tokens used in the Response.
     */
    @SerialName("audio_tokens")
    val audioTokens: Int? = null
)
