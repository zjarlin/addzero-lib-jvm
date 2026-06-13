// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToolSearchOutput(
    /**
     * The type of the item. Always `tool_search_output`.
     */
    val type: String = "tool_search_output",
    /**
     * The unique ID of the tool search output item.
     */
    val id: String,
    @SerialName("call_id")
    val callId: String?,
    /**
     * Whether tool search was executed by the server or by the client.
     */
    val execution: site.addzero.api.openai.models.ToolSearchExecutionType,
    /**
     * The loaded tool definitions returned by tool search.
     */
    val tools: List<site.addzero.api.openai.models.Tool>,
    /**
     * The status of the tool search output item that was recorded.
     */
    val status: site.addzero.api.openai.models.FunctionCallOutputStatusEnum,
    /**
     * The identifier of the actor that created the item.
     */
    @SerialName("created_by")
    val createdBy: String? = null
)
