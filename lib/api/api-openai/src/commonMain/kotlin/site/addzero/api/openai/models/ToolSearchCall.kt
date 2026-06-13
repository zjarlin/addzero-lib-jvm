// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ToolSearchCall(
    /**
     * The type of the item. Always `tool_search_call`.
     */
    val type: String = "tool_search_call",
    /**
     * The unique ID of the tool search call item.
     */
    val id: String,
    @SerialName("call_id")
    val callId: String?,
    /**
     * Whether tool search was executed by the server or by the client.
     */
    val execution: site.addzero.api.openai.models.ToolSearchExecutionType,
    /**
     * Arguments used for the tool search call.
     */
    val arguments: JsonElement,
    /**
     * The status of the tool search call item that was recorded.
     */
    val status: site.addzero.api.openai.models.FunctionCallStatus,
    /**
     * The identifier of the actor that created the item.
     */
    @SerialName("created_by")
    val createdBy: String? = null
)
