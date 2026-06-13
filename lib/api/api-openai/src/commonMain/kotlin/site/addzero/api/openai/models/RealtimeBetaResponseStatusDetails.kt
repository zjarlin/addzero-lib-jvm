// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Additional details about the status.
 */
@Serializable
data class RealtimeBetaResponseStatusDetails(
    /**
     * The type of error that caused the response to fail, corresponding with the `status` field
     * (`completed`, `cancelled`, `incomplete`, `failed`).
     */
    val type: String? = null,
    /**
     * The reason the Response did not complete. For a `cancelled` Response, one of `turn_detected` (the
     * server VAD detected a new start of speech) or `client_cancelled` (the client sent a cancel event).
     * For an `incomplete` Response, one of `max_output_tokens` or `content_filter` (the server-side safety
     * filter activated and cut off the response).
     */
    val reason: String? = null,
    /**
     * A description of the error that caused the response to fail, populated when the `status` is
     * `failed`.
     */
    val error: site.addzero.api.openai.models.RealtimeBetaResponseStatusDetailsError? = null
)
