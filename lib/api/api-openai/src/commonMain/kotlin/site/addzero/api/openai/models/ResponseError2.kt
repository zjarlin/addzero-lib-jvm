// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An error object returned when the model fails to generate a Response.
 */
@Serializable
data class ResponseError2(
    val code: site.addzero.api.openai.models.ResponseErrorCode,
    /**
     * A human-readable description of the error.
     */
    val message: String
)
