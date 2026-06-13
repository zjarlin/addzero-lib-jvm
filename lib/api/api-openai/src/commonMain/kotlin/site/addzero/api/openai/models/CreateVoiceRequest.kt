// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateVoiceRequest(
    /**
     * The name of the new voice.
     */
    val name: String,
    /**
     * The sample audio recording file. Maximum size is 10 MiB. Supported MIME types: `audio/mpeg`,
     * `audio/wav`, `audio/x-wav`, `audio/ogg`, `audio/aac`, `audio/flac`, `audio/webm`, `audio/mp4`.
     */
    @SerialName("audio_sample")
    val audioSample: ByteArray,
    /**
     * The consent recording ID (for example, `cons_1234`).
     */
    val consent: String
)
