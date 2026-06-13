// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Data about a previous audio response from the model. [Learn more](/docs/guides/audio).
 */
@Serializable
data class ChatCompletionRequestAssistantMessageAudio(
    /**
     * Unique identifier for a previous audio response from the model.
     */
    val id: String
)
