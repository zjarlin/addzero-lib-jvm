// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The delta containing the fields that have changed on the Message.
 */
@Serializable
data class MessageDeltaObjectDelta(
    /**
     * The entity that produced the message. One of `user` or `assistant`.
     */
    val role: String? = null,
    /**
     * The content of the message in array of text and/or images.
     */
    val content: List<JsonElement>? = null
)
