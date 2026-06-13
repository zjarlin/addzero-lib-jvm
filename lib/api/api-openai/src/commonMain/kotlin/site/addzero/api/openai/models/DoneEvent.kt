// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Occurs when a stream ends.
 */
@Serializable
data class DoneEvent(
    val event: String,
    val data: String
)
