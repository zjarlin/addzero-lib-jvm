// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Unconstrained free-form text.
 */
@Serializable
data class CustomToolChatCompletionsCustomFormat(
    /**
     * Unconstrained text format. Always `text`.
     */
    val type: String
)
