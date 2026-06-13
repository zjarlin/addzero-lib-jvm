// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.kcloud.api.openai

import de.jensklingenberg.ktorfit.http.*

/** Audit Logs REST endpoints. */
interface OpenAiAuditLogsApi {

    /**
     * List user actions and configuration changes within this organization.
     *
     * REST: GET /organization/audit_logs
     */
    @GET(OpenAiApiPaths.ORGANIZATION_BY_AUDIT_LOGS)
    suspend fun listAuditLogs(
        @Query("effective_at") effectiveAt: OpenAiQueryObject? = null,
        @Query("project_ids[]") projectIds: List<String>? = null,
        @Query("event_types[]") eventTypes: List<String>? = null,
        @Query("actor_ids[]") actorIds: List<String>? = null,
        @Query("actor_emails[]") actorEmails: List<String>? = null,
        @Query("resource_ids[]") resourceIds: List<String>? = null,
        @Query("limit") limit: Int? = null,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): OpenAiResponseBody
}
