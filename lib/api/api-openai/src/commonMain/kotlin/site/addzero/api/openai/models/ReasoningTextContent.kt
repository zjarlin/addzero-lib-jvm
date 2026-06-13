// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Reasoning text from the model.
 */
@Serializable
data class ReasoningTextContent(
    /**
     * The type of the reasoning text. Always `reasoning_text`.
     */
    val type: String = "reasoning_text",
    /**
     * The reasoning text from the model.
     */
    val text: String
)
