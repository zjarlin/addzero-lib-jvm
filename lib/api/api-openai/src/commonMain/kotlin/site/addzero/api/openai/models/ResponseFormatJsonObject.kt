// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * JSON object response format. An older method of generating JSON responses. Using `json_schema` is
 * recommended for models that support it. Note that the model will not generate JSON without a system
 * or user message instructing it to do so.
 */
@Serializable
data class ResponseFormatJsonObject(
    /**
     * The type of response format being defined. Always `json_object`.
     */
    val type: String
)
