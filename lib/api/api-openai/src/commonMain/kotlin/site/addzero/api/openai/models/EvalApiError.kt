// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An object representing an error response from the Eval API.
 */
@Serializable
data class EvalApiError(
    /**
     * The error code.
     */
    val code: String,
    /**
     * The error message.
     */
    val message: String
)
