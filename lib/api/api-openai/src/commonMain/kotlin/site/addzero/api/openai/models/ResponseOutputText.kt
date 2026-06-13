// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Assistant response text accompanied by optional annotations.
 */
@Serializable
data class ResponseOutputText(
    /**
     * Type discriminator that is always `output_text`.
     */
    val type: String = "output_text",
    /**
     * Assistant generated text.
     */
    val text: String,
    /**
     * Ordered list of annotations attached to the response text.
     */
    val annotations: List<JsonElement>
)
