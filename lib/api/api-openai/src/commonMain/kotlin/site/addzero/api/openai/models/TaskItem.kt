// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Task emitted by the workflow to show progress and status updates.
 */
@Serializable
data class TaskItem(
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
     * Type discriminator that is always `chatkit.task`.
     */
    val type: String = "chatkit.task",
    /**
     * Subtype for the task.
     */
    @SerialName("task_type")
    val taskType: site.addzero.api.openai.models.TaskType,
    val heading: String?,
    val summary: String?
)
