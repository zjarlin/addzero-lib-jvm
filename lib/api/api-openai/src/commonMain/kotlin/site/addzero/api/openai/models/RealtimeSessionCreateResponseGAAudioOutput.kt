// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeSessionCreateResponseGAAudioOutput(
    /**
     * The format of the output audio.
     */
    val format: site.addzero.api.openai.models.RealtimeAudioFormats? = null,
    /**
     * The voice the model uses to respond. Voice cannot be changed during the session once the model has
     * responded with audio at least once. Current voice options are `alloy`, `ash`, `ballad`, `coral`,
     * `echo`, `sage`, `shimmer`, `verse`, `marin`, and `cedar`. We recommend `marin` and `cedar` for best
     * quality.
     */
    val voice: site.addzero.api.openai.models.VoiceIdsShared? = null,
    /**
     * The speed of the model's spoken response as a multiple of the original speed. 1.0 is the default
     * speed. 0.25 is the minimum speed. 1.5 is the maximum speed. This value can only be changed in
     * between model turns, not while a response is in progress. This parameter is a post-processing
     * adjustment to the audio after it is generated, it's also possible to prompt the model to speak
     * faster or slower.
     */
    val speed: Double? = 1.0
)
