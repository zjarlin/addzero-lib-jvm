// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details of the error.
 */
@Serializable
data class RealtimeBetaServerEventErrorError(
    /**
     * The type of error (e.g., "invalid_request_error", "server_error").
     */
    val type: String,
    val code: String? = null,
    /**
     * A human-readable error message.
     */
    val message: String,
    val param: String? = null,
    @SerialName("event_id")
    val eventId: String? = null
)
