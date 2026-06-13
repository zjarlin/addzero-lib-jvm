// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Indicates that a thread is locked and cannot accept new input.
 */
@Serializable
data class LockedStatus(
    /**
     * Status discriminator that is always `locked`.
     */
    val type: String = "locked",
    val reason: String?
)
