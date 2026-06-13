// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON Schema response format. Used to generate structured JSON responses. Learn more about
 * [Structured Outputs](/docs/guides/structured-outputs).
 */
@Serializable
data class ResponseFormatJsonSchema(
    /**
     * The type of response format being defined. Always `json_schema`.
     */
    val type: String,
    /**
     * Structured Outputs configuration options, including a JSON Schema.
     */
    @SerialName("json_schema")
    val jsonSchema: site.addzero.api.openai.models.ResponseFormatJsonSchemaJsonSchema
)
