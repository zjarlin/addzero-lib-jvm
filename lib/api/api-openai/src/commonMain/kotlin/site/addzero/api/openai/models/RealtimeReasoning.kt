// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Configuration for reasoning-capable Realtime models such as `gpt-realtime-2`.
 */
@Serializable
data class RealtimeReasoning(
    val effort: site.addzero.api.openai.models.RealtimeReasoningEffort? = null
)
