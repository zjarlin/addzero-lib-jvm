// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A text output from the model.
 */
@Serializable
data class EvalItemContentOutputText(
    /**
     * The type of the output text. Always `output_text`.
     */
    val type: String,
    /**
     * The text output from the model.
     */
    val text: String
)
