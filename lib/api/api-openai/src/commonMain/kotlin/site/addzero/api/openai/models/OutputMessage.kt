// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An output message from the model.
 */
@Serializable
data class OutputMessage(
    /**
     * The unique ID of the output message.
     */
    val id: String,
    /**
     * The type of the output message. Always `message`.
     */
    val type: String,
    /**
     * The role of the output message. Always `assistant`.
     */
    val role: String,
    /**
     * The content of the output message.
     */
    val content: List<site.addzero.api.openai.models.OutputMessageContent>,
    val phase: site.addzero.api.openai.models.MessagePhase? = null,
    /**
     * The status of the message input. One of `in_progress`, `completed`, or `incomplete`. Populated when
     * input items are returned via API.
     */
    val status: String
)
