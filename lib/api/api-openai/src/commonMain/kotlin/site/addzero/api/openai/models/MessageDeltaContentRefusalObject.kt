// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The refusal content that is part of a message.
 */
@Serializable
data class MessageDeltaContentRefusalObject(
    /**
     * The index of the refusal part in the message.
     */
    val index: Int,
    /**
     * Always `refusal`.
     */
    val type: String,
    val refusal: String? = null
)
