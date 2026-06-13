// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Hosted or BYOT tool search configuration for deferred tools.
 */
@Serializable
data class ToolSearchToolParam(
    /**
     * The type of the tool. Always `tool_search`.
     */
    val type: String = "tool_search",
    /**
     * Whether tool search is executed by the server or by the client.
     */
    val execution: site.addzero.api.openai.models.ToolSearchExecutionType? = null,
    val description: String? = null,
    val parameters: site.addzero.api.openai.models.EmptyModelParam? = null
)
