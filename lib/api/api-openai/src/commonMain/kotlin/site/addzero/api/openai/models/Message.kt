// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A message to or from the model.
 */
@Serializable
data class Message(
    /**
     * The type of the message. Always set to `message`.
     */
    val type: String = "message",
    /**
     * The unique ID of the message.
     */
    val id: String,
    /**
     * The status of item. One of `in_progress`, `completed`, or `incomplete`. Populated when items are
     * returned via API.
     */
    val status: site.addzero.api.openai.models.MessageStatus,
    /**
     * The role of the message. One of `unknown`, `user`, `assistant`, `system`, `critic`, `discriminator`,
     * `developer`, or `tool`.
     */
    val role: site.addzero.api.openai.models.MessageRole,
    /**
     * The content of the message
     */
    val content: List<JsonElement>,
    val phase: site.addzero.api.openai.models.MessagePhase2? = null
)
