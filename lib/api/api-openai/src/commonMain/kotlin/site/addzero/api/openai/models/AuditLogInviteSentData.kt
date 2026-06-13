// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The payload used to create the invite.
 */
@Serializable
data class AuditLogInviteSentData(
    /**
     * The email invited to the organization.
     */
    val email: String? = null,
    /**
     * The role the email was invited to be. Is either `owner` or `member`.
     */
    val role: String? = null
)
