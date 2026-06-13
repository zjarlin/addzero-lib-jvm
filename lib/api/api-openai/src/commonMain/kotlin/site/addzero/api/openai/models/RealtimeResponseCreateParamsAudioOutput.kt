// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeResponseCreateParamsAudioOutput(
    /**
     * The format of the output audio.
     */
    val format: site.addzero.api.openai.models.RealtimeAudioFormats? = null,
    /**
     * The voice the model uses to respond. Supported built-in voices are `alloy`, `ash`, `ballad`,
     * `coral`, `echo`, `sage`, `shimmer`, `verse`, `marin`, and `cedar`. You may also provide a custom
     * voice object with an `id`, for example `{ "id": "voice_1234" }`. Voice cannot be changed during the
     * session once the model has responded with audio at least once. We recommend `marin` and `cedar` for
     * best quality.
     */
    val voice: site.addzero.api.openai.models.VoiceIdsOrCustomVoice? = null
)
