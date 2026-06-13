// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToolSearchCallItemParam(
    val id: String? = null,
    @SerialName("call_id")
    val callId: String? = null,
    /**
     * The item type. Always `tool_search_call`.
     */
    val type: String = "tool_search_call",
    /**
     * Whether tool search was executed by the server or by the client.
     */
    val execution: site.addzero.api.openai.models.ToolSearchExecutionType? = null,
    /**
     * The arguments supplied to the tool search call.
     */
    val arguments: site.addzero.api.openai.models.EmptyModelParam,
    val status: site.addzero.api.openai.models.FunctionCallItemStatus? = null
)
