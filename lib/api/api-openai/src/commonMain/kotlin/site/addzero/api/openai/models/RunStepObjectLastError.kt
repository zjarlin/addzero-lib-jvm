// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The last error associated with this run step. Will be `null` if there are no errors.
 */
@Serializable
data class RunStepObjectLastError(
    /**
     * One of `server_error` or `rate_limit_exceeded`.
     */
    val code: String,
    /**
     * A human-readable description of the error.
     */
    val message: String
)
