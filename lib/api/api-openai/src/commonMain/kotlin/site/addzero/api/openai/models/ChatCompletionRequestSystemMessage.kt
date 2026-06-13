// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Developer-provided instructions that the model should follow, regardless of messages sent by the
 * user. With o1 models and newer, use `developer` messages for this purpose instead.
 */
@Serializable
data class ChatCompletionRequestSystemMessage(
    /**
     * The contents of the system message.
     */
    val content: JsonElement,
    /**
     * The role of the messages author, in this case `system`.
     */
    val role: String,
    /**
     * An optional name for the participant. Provides the model information to differentiate between
     * participants of the same role.
     */
    val name: String? = null
)
