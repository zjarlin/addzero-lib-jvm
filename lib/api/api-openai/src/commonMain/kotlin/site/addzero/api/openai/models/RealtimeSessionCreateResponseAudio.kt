// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for input and output audio for the session.
 */
@Serializable
data class RealtimeSessionCreateResponseAudio(
    val input: site.addzero.api.openai.models.RealtimeSessionCreateResponseAudioInput? = null,
    val output: site.addzero.api.openai.models.RealtimeSessionCreateResponseAudioOutput? = null
)
