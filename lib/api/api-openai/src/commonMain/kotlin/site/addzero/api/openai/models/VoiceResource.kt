// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A custom voice that can be used for audio output.
 */
@Serializable
data class VoiceResource(
    /**
     * The object type, which is always `audio.voice`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The voice identifier, which can be referenced in API endpoints.
     */
    val id: String,
    /**
     * The name of the voice.
     */
    val name: String,
    /**
     * The Unix timestamp (in seconds) for when the voice was created.
     */
    @SerialName("created_at")
    val createdAt: Long
)
