// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateVoiceConsentRequest(
    /**
     * The label to use for this consent recording.
     */
    val name: String,
    /**
     * The consent audio recording file. Maximum size is 10 MiB. Supported MIME types: `audio/mpeg`,
     * `audio/wav`, `audio/x-wav`, `audio/ogg`, `audio/aac`, `audio/flac`, `audio/webm`, `audio/mp4`.
     */
    val recording: ByteArray,
    /**
     * The BCP 47 language tag for the consent phrase (for example, `en-US`).
     */
    val language: String
)
