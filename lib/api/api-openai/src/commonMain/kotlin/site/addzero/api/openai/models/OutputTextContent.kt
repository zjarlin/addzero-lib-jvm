// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A text output from the model.
 */
@Serializable
data class OutputTextContent(
    /**
     * The type of the output text. Always `output_text`.
     */
    val type: String = "output_text",
    /**
     * The text output from the model.
     */
    val text: String,
    /**
     * The annotations of the text output.
     */
    val annotations: List<site.addzero.api.openai.models.Annotation>,
    val logprobs: List<site.addzero.api.openai.models.LogProb>
)
