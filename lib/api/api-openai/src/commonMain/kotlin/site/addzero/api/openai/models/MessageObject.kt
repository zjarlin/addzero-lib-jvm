// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a message within a [thread](/docs/api-reference/threads).
 */
@Serializable
data class MessageObject(
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The object type, which is always `thread.message`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The Unix timestamp (in seconds) for when the message was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The [thread](/docs/api-reference/threads) ID that this message belongs to.
     */
    @SerialName("thread_id")
    val threadId: String,
    /**
     * The status of the message, which can be either `in_progress`, `incomplete`, or `completed`.
     */
    val status: String,
    @SerialName("incomplete_details")
    val incompleteDetails: site.addzero.api.openai.models.MessageObjectIncompleteDetails?,
    @SerialName("completed_at")
    val completedAt: Long?,
    @SerialName("incomplete_at")
    val incompleteAt: Long?,
    /**
     * The entity that produced the message. One of `user` or `assistant`.
     */
    val role: String,
    /**
     * The content of the message in array of text and/or images.
     */
    val content: List<JsonElement>,
    @SerialName("assistant_id")
    val assistantId: String?,
    @SerialName("run_id")
    val runId: String?,
    val attachments: List<site.addzero.api.openai.models.MessageObjectAttachment>?,
    val metadata: site.addzero.api.openai.models.Metadata?
)
