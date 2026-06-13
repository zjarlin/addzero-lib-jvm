// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The last error associated with this run. Will be `null` if there are no errors.
 */
@Serializable
data class RunObjectLastError(
    /**
     * One of `server_error`, `rate_limit_exceeded`, or `invalid_prompt`.
     */
    val code: String,
    /**
     * A human-readable description of the error.
     */
    val message: String
)
