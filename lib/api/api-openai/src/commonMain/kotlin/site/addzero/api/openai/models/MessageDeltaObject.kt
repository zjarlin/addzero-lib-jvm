// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a message delta i.e. any changed fields on a message during streaming.
 */
@Serializable
data class MessageDeltaObject(
    /**
     * The identifier of the message, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The object type, which is always `thread.message.delta`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The delta containing the fields that have changed on the Message.
     */
    val delta: site.addzero.api.openai.models.MessageDeltaObjectDelta
)
