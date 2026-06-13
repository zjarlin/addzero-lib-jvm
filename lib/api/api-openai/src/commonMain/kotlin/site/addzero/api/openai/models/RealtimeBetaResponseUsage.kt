// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Usage statistics for the Response, this will correspond to billing. A Realtime API session will
 * maintain a conversation context and append new Items to the Conversation, thus output from previous
 * turns (text and audio tokens) will become the input for later turns.
 */
@Serializable
data class RealtimeBetaResponseUsage(
    /**
     * The total number of tokens in the Response including input and output text and audio tokens.
     */
    @SerialName("total_tokens")
    val totalTokens: Int? = null,
    /**
     * The number of input tokens used in the Response, including text and audio tokens.
     */
    @SerialName("input_tokens")
    val inputTokens: Int? = null,
    /**
     * The number of output tokens sent in the Response, including text and audio tokens.
     */
    @SerialName("output_tokens")
    val outputTokens: Int? = null,
    /**
     * Details about the input tokens used in the Response.
     */
    @SerialName("input_token_details")
    val inputTokenDetails: site.addzero.api.openai.models.RealtimeBetaResponseUsageInputTokenDetails? = null,
    /**
     * Details about the output tokens used in the Response.
     */
    @SerialName("output_token_details")
    val outputTokenDetails: site.addzero.api.openai.models.RealtimeBetaResponseUsageOutputTokenDetails? = null
)
