// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Indicates that a thread has been closed.
 */
@Serializable
data class ClosedStatus(
    /**
     * Status discriminator that is always `closed`.
     */
    val type: String = "closed",
    val reason: String?
)
