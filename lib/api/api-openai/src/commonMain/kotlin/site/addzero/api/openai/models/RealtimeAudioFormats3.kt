// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The G.711 μ-law format.
 */
@Serializable
data class RealtimeAudioFormats3(
    /**
     * The audio format. Always `audio/pcmu`.
     */
    val type: String? = null
)
