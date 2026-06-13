// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An error that occurred while generating the response.
 */
@Serializable
data class Error2(
    /**
     * A machine-readable error code that was returned.
     */
    val code: String,
    /**
     * A human-readable description of the error that was returned.
     */
    val message: String
)
