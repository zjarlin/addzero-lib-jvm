// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeSessionCreateResponseAudioOutput(
    val format: site.addzero.api.openai.models.RealtimeAudioFormats? = null,
    val voice: site.addzero.api.openai.models.VoiceIdsShared? = null,
    val speed: Double? = null
)
