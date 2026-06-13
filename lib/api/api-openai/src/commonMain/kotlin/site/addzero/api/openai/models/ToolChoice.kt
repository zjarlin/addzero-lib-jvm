// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Tool selection that the assistant should honor when executing the item.
 */
@Serializable
data class ToolChoice(
    /**
     * Identifier of the requested tool.
     */
    val id: String
)
