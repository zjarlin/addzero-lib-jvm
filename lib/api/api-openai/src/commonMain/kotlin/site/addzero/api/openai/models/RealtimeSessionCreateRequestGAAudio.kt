// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for input and output audio.
 */
@Serializable
data class RealtimeSessionCreateRequestGAAudio(
    val input: site.addzero.api.openai.models.RealtimeSessionCreateRequestGAAudioInput? = null,
    val output: site.addzero.api.openai.models.RealtimeSessionCreateRequestGAAudioOutput? = null
)
