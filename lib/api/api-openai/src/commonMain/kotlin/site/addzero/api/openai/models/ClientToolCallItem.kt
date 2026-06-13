// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Record of a client side tool invocation initiated by the assistant.
 */
@Serializable
data class ClientToolCallItem(
    /**
     * Identifier of the thread item.
     */
    val id: String,
    /**
     * Type discriminator that is always `chatkit.thread_item`.
     */
    @SerialName("object")
    val objectType: String = "chatkit.thread_item",
    /**
     * Unix timestamp (in seconds) for when the item was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * Identifier of the parent thread.
     */
    @SerialName("thread_id")
    val threadId: String,
    /**
     * Type discriminator that is always `chatkit.client_tool_call`.
     */
    val type: String = "chatkit.client_tool_call",
    /**
     * Execution status for the tool call.
     */
    val status: site.addzero.api.openai.models.ClientToolCallStatus,
    /**
     * Identifier for the client tool call.
     */
    @SerialName("call_id")
    val callId: String,
    /**
     * Tool name that was invoked.
     */
    val name: String,
    /**
     * JSON-encoded arguments that were sent to the tool.
     */
    val arguments: String,
    val output: String?
)
