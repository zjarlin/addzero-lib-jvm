// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Usage statistics for models billed by audio input duration.
 */
@Serializable
data class TranscriptTextUsageDuration(
    /**
     * The type of the usage object. Always `duration` for this variant.
     */
    val type: String,
    /**
     * Duration of the input audio in seconds.
     */
    val seconds: Double
)
