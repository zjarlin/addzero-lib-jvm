// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Indicates that a thread is active.
 */
@Serializable
data class ActiveStatus(
    /**
     * Status discriminator that is always `active`.
     */
    val type: String = "active"
)
