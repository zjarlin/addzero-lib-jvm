// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The summary part that was added.
 */
@Serializable
data class ResponseReasoningSummaryPartAddedEventPart(
    /**
     * The type of the summary part. Always `summary_text`.
     */
    val type: String,
    /**
     * The text of the summary part.
     */
    val text: String
)
