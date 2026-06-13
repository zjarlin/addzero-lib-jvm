// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for input and output audio.
 */
@Serializable
data class RealtimeSessionCreateResponseGAAudio(
    val input: site.addzero.api.openai.models.RealtimeSessionCreateResponseGAAudioInput? = null,
    val output: site.addzero.api.openai.models.RealtimeSessionCreateResponseGAAudioOutput? = null
)
