// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The conversation that this response belonged to. Input items and output items from this response
 * were automatically added to this conversation.
 */
@Serializable
data class Conversation2(
    /**
     * The unique ID of the conversation that this response was associated with.
     */
    val id: String
)
