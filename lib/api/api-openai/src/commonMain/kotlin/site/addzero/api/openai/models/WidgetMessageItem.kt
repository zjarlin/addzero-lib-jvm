// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Thread item that renders a widget payload.
 */
@Serializable
data class WidgetMessageItem(
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
     * Type discriminator that is always `chatkit.widget`.
     */
    val type: String = "chatkit.widget",
    /**
     * Serialized widget payload rendered in the UI.
     */
    val widget: String
)
