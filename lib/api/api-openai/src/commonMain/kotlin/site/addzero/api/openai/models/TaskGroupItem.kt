// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Collection of workflow tasks grouped together in the thread.
 */
@Serializable
data class TaskGroupItem(
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
     * Type discriminator that is always `chatkit.task_group`.
     */
    val type: String = "chatkit.task_group",
    /**
     * Tasks included in the group.
     */
    val tasks: List<site.addzero.api.openai.models.TaskGroupTask>
)
