// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The G.711 A-law format.
 */
@Serializable
data class RealtimeAudioFormats4(
    /**
     * The audio format. Always `audio/pcma`.
     */
    val type: String? = null
)
