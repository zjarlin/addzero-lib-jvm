// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an individual `invite` to the organization.
 */
@Serializable
data class Invite(
    /**
     * The object type, which is always `organization.invite`
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    /**
     * The email address of the individual to whom the invite was sent
     */
    val email: String,
    /**
     * `owner` or `reader`
     */
    val role: String,
    /**
     * `accepted`,`expired`, or `pending`
     */
    val status: String,
    /**
     * The Unix timestamp (in seconds) of when the invite was sent.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * The Unix timestamp (in seconds) of when the invite expires.
     */
    @SerialName("expires_at")
    val expiresAt: Long? = null,
    /**
     * The Unix timestamp (in seconds) of when the invite was accepted.
     */
    @SerialName("accepted_at")
    val acceptedAt: Long? = null,
    /**
     * The projects that were granted membership upon acceptance of the invite.
     */
    val projects: List<site.addzero.api.openai.models.InviteProject>
)
