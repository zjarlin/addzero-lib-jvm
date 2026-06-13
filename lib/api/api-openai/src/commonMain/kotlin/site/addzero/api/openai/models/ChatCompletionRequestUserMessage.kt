// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Messages sent by an end user, containing prompts or additional context information.
 */
@Serializable
data class ChatCompletionRequestUserMessage(
    /**
     * The contents of the user message.
     */
    val content: JsonElement,
    /**
     * The role of the messages author, in this case `user`.
     */
    val role: String,
    /**
     * An optional name for the participant. Provides the model information to differentiate between
     * participants of the same role.
     */
    val name: String? = null
)
