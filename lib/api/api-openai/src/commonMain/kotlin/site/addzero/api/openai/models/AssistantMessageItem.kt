// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Assistant-authored message within a thread.
 */
@Serializable
data class AssistantMessageItem(
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
     * Type discriminator that is always `chatkit.assistant_message`.
     */
    val type: String = "chatkit.assistant_message",
    /**
     * Ordered assistant response segments.
     */
    val content: List<site.addzero.api.openai.models.ResponseOutputText>
)
