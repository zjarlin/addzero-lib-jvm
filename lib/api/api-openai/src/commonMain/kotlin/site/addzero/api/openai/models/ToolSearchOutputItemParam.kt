// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToolSearchOutputItemParam(
    val id: String? = null,
    @SerialName("call_id")
    val callId: String? = null,
    /**
     * The item type. Always `tool_search_output`.
     */
    val type: String = "tool_search_output",
    /**
     * Whether tool search was executed by the server or by the client.
     */
    val execution: site.addzero.api.openai.models.ToolSearchExecutionType? = null,
    /**
     * The loaded tool definitions returned by the tool search output.
     */
    val tools: List<site.addzero.api.openai.models.Tool>,
    val status: site.addzero.api.openai.models.FunctionCallItemStatus? = null
)
