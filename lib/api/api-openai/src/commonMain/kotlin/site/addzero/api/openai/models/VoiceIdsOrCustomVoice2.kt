// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Custom voice reference.
 */
@Serializable
data class VoiceIdsOrCustomVoice2(
    /**
     * The custom voice ID, e.g. `voice_1234`.
     */
    val id: String
)
