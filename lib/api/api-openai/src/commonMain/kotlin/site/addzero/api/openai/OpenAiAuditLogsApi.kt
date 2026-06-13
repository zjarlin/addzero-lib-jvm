// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai

import de.jensklingenberg.ktorfit.http.*
import site.addzero.api.openai.models.AuditLogEventType
import site.addzero.api.openai.models.EffectiveAtParameter
import site.addzero.api.openai.models.ListAuditLogsResponse

interface OpenAiAuditLogsApi {

    /**
     * List user actions and configuration changes within this organization. REST: GET
     * /organization/audit_logs
     */
    @GET(_root_ide_package_.site.addzero.api.openai.OpenAiApiPaths.ORGANIZATION_BY_AUDIT_LOGS)
    suspend fun listAuditLogs(
        @Query("effective_at") effectiveAt: site.addzero.api.openai.models.EffectiveAtParameter? = null,
        @Query("project_ids[]") projectIds: List<String>? = null,
        @Query("event_types[]") eventTypes: List<site.addzero.api.openai.models.AuditLogEventType>? = null,
        @Query("actor_ids[]") actorIds: List<String>? = null,
        @Query("actor_emails[]") actorEmails: List<String>? = null,
        @Query("resource_ids[]") resourceIds: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): site.addzero.api.openai.models.ListAuditLogsResponse

}
