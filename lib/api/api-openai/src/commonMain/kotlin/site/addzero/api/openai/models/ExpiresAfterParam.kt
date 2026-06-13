// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Controls when the session expires relative to an anchor timestamp.
 */
@Serializable
data class ExpiresAfterParam(
    /**
     * Base timestamp used to calculate expiration. Currently fixed to `created_at`.
     */
    val anchor: String = "created_at",
    /**
     * Number of seconds after the anchor when the session expires.
     */
    val seconds: Long
)
