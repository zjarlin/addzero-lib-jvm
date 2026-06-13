// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A grammar defined by the user.
 */
@Serializable
data class CustomToolChatCompletionsCustomFormat2(
    /**
     * Grammar format. Always `grammar`.
     */
    val type: String,
    /**
     * Your chosen grammar.
     */
    val grammar: site.addzero.api.openai.models.CustomToolChatCompletionsCustomFormat2Grammar
)
