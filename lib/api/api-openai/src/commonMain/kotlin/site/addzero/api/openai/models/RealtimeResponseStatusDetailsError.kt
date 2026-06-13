// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A description of the error that caused the response to fail, populated when the `status` is
 * `failed`.
 */
@Serializable
data class RealtimeResponseStatusDetailsError(
    /**
     * The type of error.
     */
    val type: String? = null,
    /**
     * Error code, if any.
     */
    val code: String? = null
)
