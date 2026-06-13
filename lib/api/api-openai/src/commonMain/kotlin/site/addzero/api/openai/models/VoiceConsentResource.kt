// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A consent recording used to authorize creation of a custom voice.
 */
@Serializable
data class VoiceConsentResource(
    /**
     * The object type, which is always `audio.voice_consent`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The consent recording identifier.
     */
    val id: String,
    /**
     * The label provided when the consent recording was uploaded.
     */
    val name: String,
    /**
     * The BCP 47 language tag for the consent phrase (for example, `en-US`).
     */
    val language: String,
    /**
     * The Unix timestamp (in seconds) for when the consent recording was created.
     */
    @SerialName("created_at")
    val createdAt: Long
)
