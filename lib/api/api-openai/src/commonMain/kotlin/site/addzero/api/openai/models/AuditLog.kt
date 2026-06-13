// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A log of a user action or configuration change within this organization.
 */
@Serializable
data class AuditLog(
    /**
     * The ID of this log.
     */
    val id: String,
    val type: site.addzero.api.openai.models.AuditLogEventType,
    /**
     * The Unix timestamp (in seconds) of the event.
     */
    @SerialName("effective_at")
    val effectiveAt: Long,
    /**
     * The project that the action was scoped to. Absent for actions not scoped to projects. Note that any
     * admin actions taken via Admin API keys are associated with the default project.
     */
    val project: site.addzero.api.openai.models.AuditLogProject? = null,
    val actor: site.addzero.api.openai.models.AuditLogActor? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("api_key.created")
    val apiKeyCreated: site.addzero.api.openai.models.AuditLogApiKeyCreated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("api_key.updated")
    val apiKeyUpdated: site.addzero.api.openai.models.AuditLogApiKeyUpdated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("api_key.deleted")
    val apiKeyDeleted: site.addzero.api.openai.models.AuditLogApiKeyDeleted? = null,
    /**
     * The project and fine-tuned model checkpoint that the checkpoint permission was created for.
     */
    @SerialName("checkpoint.permission.created")
    val checkpointPermissionCreated: site.addzero.api.openai.models.AuditLogCheckpointPermissionCreated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("checkpoint.permission.deleted")
    val checkpointPermissionDeleted: site.addzero.api.openai.models.AuditLogCheckpointPermissionDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("external_key.registered")
    val externalKeyRegistered: site.addzero.api.openai.models.AuditLogExternalKeyRegistered? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("external_key.removed")
    val externalKeyRemoved: site.addzero.api.openai.models.AuditLogExternalKeyRemoved? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("group.created")
    val groupCreated: site.addzero.api.openai.models.AuditLogGroupCreated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("group.updated")
    val groupUpdated: site.addzero.api.openai.models.AuditLogGroupUpdated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("group.deleted")
    val groupDeleted: site.addzero.api.openai.models.AuditLogGroupDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("scim.enabled")
    val scimEnabled: site.addzero.api.openai.models.AuditLogScimEnabled? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("scim.disabled")
    val scimDisabled: site.addzero.api.openai.models.AuditLogScimDisabled? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("invite.sent")
    val inviteSent: site.addzero.api.openai.models.AuditLogInviteSent? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("invite.accepted")
    val inviteAccepted: site.addzero.api.openai.models.AuditLogInviteAccepted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("invite.deleted")
    val inviteDeleted: site.addzero.api.openai.models.AuditLogInviteDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("ip_allowlist.created")
    val ipAllowlistCreated: site.addzero.api.openai.models.AuditLogIpAllowlistCreated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("ip_allowlist.updated")
    val ipAllowlistUpdated: site.addzero.api.openai.models.AuditLogIpAllowlistUpdated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("ip_allowlist.deleted")
    val ipAllowlistDeleted: site.addzero.api.openai.models.AuditLogIpAllowlistDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("ip_allowlist.config.activated")
    val ipAllowlistConfigActivated: site.addzero.api.openai.models.AuditLogIpAllowlistConfigActivated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("ip_allowlist.config.deactivated")
    val ipAllowlistConfigDeactivated: site.addzero.api.openai.models.AuditLogIpAllowlistConfigDeactivated? = null,
    /**
     * This event has no additional fields beyond the standard audit log attributes.
     */
    @SerialName("login.succeeded")
    val loginSucceeded: Map<String, JsonElement>? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("login.failed")
    val loginFailed: site.addzero.api.openai.models.AuditLogLoginFailed? = null,
    /**
     * This event has no additional fields beyond the standard audit log attributes.
     */
    @SerialName("logout.succeeded")
    val logoutSucceeded: Map<String, JsonElement>? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("logout.failed")
    val logoutFailed: site.addzero.api.openai.models.AuditLogLogoutFailed? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("organization.updated")
    val organizationUpdated: site.addzero.api.openai.models.AuditLogOrganizationUpdated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("project.created")
    val projectCreated: site.addzero.api.openai.models.AuditLogProjectCreated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("project.updated")
    val projectUpdated: site.addzero.api.openai.models.AuditLogProjectUpdated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("project.archived")
    val projectArchived: site.addzero.api.openai.models.AuditLogProjectArchived? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("project.deleted")
    val projectDeleted: site.addzero.api.openai.models.AuditLogProjectDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("rate_limit.updated")
    val rateLimitUpdated: site.addzero.api.openai.models.AuditLogRateLimitUpdated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("rate_limit.deleted")
    val rateLimitDeleted: site.addzero.api.openai.models.AuditLogRateLimitDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("role.created")
    val roleCreated: site.addzero.api.openai.models.AuditLogRoleCreated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("role.updated")
    val roleUpdated: site.addzero.api.openai.models.AuditLogRoleUpdated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("role.deleted")
    val roleDeleted: site.addzero.api.openai.models.AuditLogRoleDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("role.assignment.created")
    val roleAssignmentCreated: site.addzero.api.openai.models.AuditLogRoleAssignmentCreated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("role.assignment.deleted")
    val roleAssignmentDeleted: site.addzero.api.openai.models.AuditLogRoleAssignmentDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("service_account.created")
    val serviceAccountCreated: site.addzero.api.openai.models.AuditLogServiceAccountCreated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("service_account.updated")
    val serviceAccountUpdated: site.addzero.api.openai.models.AuditLogServiceAccountUpdated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("service_account.deleted")
    val serviceAccountDeleted: site.addzero.api.openai.models.AuditLogServiceAccountDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("user.added")
    val userAdded: site.addzero.api.openai.models.AuditLogUserAdded? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("user.updated")
    val userUpdated: site.addzero.api.openai.models.AuditLogUserUpdated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("user.deleted")
    val userDeleted: site.addzero.api.openai.models.AuditLogUserDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("certificate.created")
    val certificateCreated: site.addzero.api.openai.models.AuditLogCertificateCreated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("certificate.updated")
    val certificateUpdated: site.addzero.api.openai.models.AuditLogCertificateUpdated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("certificate.deleted")
    val certificateDeleted: site.addzero.api.openai.models.AuditLogCertificateDeleted? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("certificates.activated")
    val certificatesActivated: site.addzero.api.openai.models.AuditLogCertificatesActivated? = null,
    /**
     * The details for events with this `type`.
     */
    @SerialName("certificates.deactivated")
    val certificatesDeactivated: site.addzero.api.openai.models.AuditLogCertificatesDeactivated? = null
)
