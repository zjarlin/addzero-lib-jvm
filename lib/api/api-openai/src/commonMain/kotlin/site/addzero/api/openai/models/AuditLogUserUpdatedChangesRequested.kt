// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The payload used to update the user.
 */
@Serializable
data class AuditLogUserUpdatedChangesRequested(
    /**
     * The role of the user. Is either `owner` or `member`.
     */
    val role: String? = null
)
