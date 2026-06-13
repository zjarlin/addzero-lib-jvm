// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a ChatKit thread and its current status.
 */
@Serializable
data class ThreadResource(
    /**
     * Identifier of the thread.
     */
    val id: String,
    /**
     * Type discriminator that is always `chatkit.thread`.
     */
    @SerialName("object")
    val objectType: String = "chatkit.thread",
    /**
     * Unix timestamp (in seconds) for when the thread was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    val title: String?,
    /**
     * Current status for the thread. Defaults to `active` for newly created threads.
     */
    val status: JsonElement,
    /**
     * Free-form string that identifies your end user who owns the thread.
     */
    val user: String
)
