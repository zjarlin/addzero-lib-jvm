// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Parameters for audio output. Required when audio output is requested with `modalities: ["audio"]`.
 * [Learn more](/docs/guides/audio).
 */
@Serializable
data class CreateChatCompletionRequestAudio(
    /**
     * The voice the model uses to respond. Supported built-in voices are `alloy`, `ash`, `ballad`,
     * `coral`, `echo`, `fable`, `nova`, `onyx`, `sage`, `shimmer`, `marin`, and `cedar`. You may also
     * provide a custom voice object with an `id`, for example `{ "id": "voice_1234" }`.
     */
    val voice: site.addzero.api.openai.models.VoiceIdsOrCustomVoice,
    /**
     * Specifies the output audio format. Must be one of `wav`, `mp3`, `flac`, `opus`, or `pcm16`.
     */
    val format: String
)
