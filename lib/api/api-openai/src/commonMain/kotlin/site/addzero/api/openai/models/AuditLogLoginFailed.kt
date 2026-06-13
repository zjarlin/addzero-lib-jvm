// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogLoginFailed(
    /**
     * The error code of the failure.
     */
    @SerialName("error_code")
    val errorCode: String? = null,
    /**
     * The error message of the failure.
     */
    @SerialName("error_message")
    val errorMessage: String? = null
)
