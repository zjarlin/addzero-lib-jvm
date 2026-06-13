// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The payload used to create the service account.
 */
@Serializable
data class AuditLogServiceAccountCreatedData(
    /**
     * The role of the service account. Is either `owner` or `member`.
     */
    val role: String? = null
)
