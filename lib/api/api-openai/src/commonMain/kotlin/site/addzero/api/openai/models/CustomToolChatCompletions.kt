// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A custom tool that processes input using a specified format.
 */
@Serializable
data class CustomToolChatCompletions(
    /**
     * The type of the custom tool. Always `custom`.
     */
    val type: String,
    /**
     * Properties of the custom tool.
     */
    val custom: site.addzero.api.openai.models.CustomToolChatCompletionsCustom
)
