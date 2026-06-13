// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Default response format. Used to generate text responses.
 */
@Serializable
data class ResponseFormatText(
    /**
     * The type of response format being defined. Always `text`.
     */
    val type: String
)
