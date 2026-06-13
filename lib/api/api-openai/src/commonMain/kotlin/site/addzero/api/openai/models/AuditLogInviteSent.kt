// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogInviteSent(
    /**
     * The ID of the invite.
     */
    val id: String? = null,
    /**
     * The payload used to create the invite.
     */
    val data: site.addzero.api.openai.models.AuditLogInviteSentData? = null
)
