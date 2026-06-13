// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * User-authored messages within a thread.
 */
@Serializable
data class UserMessageItem(
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
    val type: String = "chatkit.user_message",
    /**
     * Ordered content elements supplied by the user.
     */
    val content: List<JsonElement>,
    /**
     * Attachments associated with the user message. Defaults to an empty list.
     */
    val attachments: List<site.addzero.api.openai.models.Attachment>,
    @SerialName("inference_options")
    val inferenceOptions: site.addzero.api.openai.models.InferenceOptions?
)
