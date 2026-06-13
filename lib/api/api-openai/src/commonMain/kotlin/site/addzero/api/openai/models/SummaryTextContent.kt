// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A summary text from the model.
 */
@Serializable
data class SummaryTextContent(
    /**
     * The type of the object. Always `summary_text`.
     */
    val type: String = "summary_text",
    /**
     * A summary of the reasoning output from the model so far.
     */
    val text: String
)
