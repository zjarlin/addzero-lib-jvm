// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * JSON Schema response format. Used to generate structured JSON responses. Learn more about
 * [Structured Outputs](/docs/guides/structured-outputs).
 */
@Serializable
data class TextResponseFormatJsonSchema(
    /**
     * The type of response format being defined. Always `json_schema`.
     */
    val type: String,
    /**
     * A description of what the response format is for, used by the model to determine how to respond in
     * the format.
     */
    val description: String? = null,
    /**
     * The name of the response format. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a
     * maximum length of 64.
     */
    val name: String,
    val schema: site.addzero.api.openai.models.ResponseFormatJsonSchemaSchema,
    val strict: Boolean? = null
)
