// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSpeechRequest(
    /**
     * One of the available [TTS models](/docs/models#tts): `tts-1`, `tts-1-hd`, `gpt-4o-mini-tts`, or
     * `gpt-4o-mini-tts-2025-12-15`.
     */
    val model: String,
    /**
     * The text to generate audio for. The maximum length is 4096 characters.
     */
    val input: String,
    /**
     * Control the voice of your generated audio with additional instructions. Does not work with `tts-1`
     * or `tts-1-hd`.
     */
    val instructions: String? = null,
    /**
     * The voice to use when generating the audio. Supported built-in voices are `alloy`, `ash`, `ballad`,
     * `coral`, `echo`, `fable`, `onyx`, `nova`, `sage`, `shimmer`, `verse`, `marin`, and `cedar`. You may
     * also provide a custom voice object with an `id`, for example `{ "id": "voice_1234" }`. Previews of
     * the voices are available in the [Text to speech guide](/docs/guides/text-to-speech#voice-options).
     */
    val voice: site.addzero.api.openai.models.VoiceIdsOrCustomVoice,
    /**
     * The format to audio in. Supported formats are `mp3`, `opus`, `aac`, `flac`, `wav`, and `pcm`.
     */
    @SerialName("response_format")
    val responseFormat: String? = "mp3",
    /**
     * The speed of the generated audio. Select a value from `0.25` to `4.0`. `1.0` is the default.
     */
    val speed: Double? = 1.0,
    /**
     * The format to stream the audio in. Supported formats are `sse` and `audio`. `sse` is not supported
     * for `tts-1` or `tts-1-hd`.
     */
    @SerialName("stream_format")
    val streamFormat: String? = "audio"
)
