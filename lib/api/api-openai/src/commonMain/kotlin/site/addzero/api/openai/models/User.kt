// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an individual `user` within an organization.
 */
@Serializable
data class User(
    /**
     * The object type, which is always `organization.user`
     */
    @SerialName("object")
    val objectType: String,
    /**
     * The identifier, which can be referenced in API endpoints
     */
    val id: String,
    /**
     * The name of the user
     */
    val name: String? = null,
    /**
     * The email address of the user
     */
    val email: String? = null,
    /**
     * `owner` or `reader`
     */
    val role: String? = null,
    /**
     * The Unix timestamp (in seconds) of when the user was added.
     */
    @SerialName("added_at")
    val addedAt: Long,
    /**
     * Whether this is the organization's default user.
     */
    @SerialName("is_default")
    val isDefault: Boolean? = null,
    /**
     * The Unix timestamp (in seconds) of when the user was created.
     */
    val created: Long? = null,
    /**
     * Nested user details.
     */
    val user: site.addzero.api.openai.models.UserUser? = null,
    /**
     * Whether the user is a service account.
     */
    @SerialName("is_service_account")
    val isServiceAccount: Boolean? = null,
    /**
     * Whether the user is an authorized purchaser for Scale Tier.
     */
    @SerialName("is_scale_tier_authorized_purchaser")
    val isScaleTierAuthorizedPurchaser: Boolean? = null,
    /**
     * Whether the user is managed through SCIM.
     */
    @SerialName("is_scim_managed")
    val isScimManaged: Boolean? = null,
    /**
     * The Unix timestamp (in seconds) of the user's last API key usage.
     */
    @SerialName("api_key_last_used_at")
    val apiKeyLastUsedAt: Long? = null,
    /**
     * The technical level metadata for the user.
     */
    @SerialName("technical_level")
    val technicalLevel: String? = null,
    /**
     * The developer persona metadata for the user.
     */
    @SerialName("developer_persona")
    val developerPersona: String? = null,
    /**
     * Projects associated with the user, if included.
     */
    val projects: site.addzero.api.openai.models.UserProjects? = null
)
