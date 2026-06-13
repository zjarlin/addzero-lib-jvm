// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A text input to the model.
 */
@Serializable
data class InputTextContentParam(
    /**
     * The type of the input item. Always `input_text`.
     */
    val type: String = "input_text",
    /**
     * The text input to the model.
     */
    val text: String
)
