// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Container expiration time in seconds relative to the 'anchor' time.
 */
@Serializable
data class CreateContainerBodyExpiresAfter(
    /**
     * Time anchor for the expiration time. Currently only 'last_active_at' is supported.
     */
    val anchor: String,
    val minutes: Int
)
