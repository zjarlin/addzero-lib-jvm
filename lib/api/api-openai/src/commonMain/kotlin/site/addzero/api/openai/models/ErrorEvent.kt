// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Occurs when an [error](/docs/guides/error-codes#api-errors) occurs. This can happen due to an
 * internal server error or a timeout.
 */
@Serializable
data class ErrorEvent(
    val event: String,
    val data: site.addzero.api.openai.models.Error
)
