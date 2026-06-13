// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The payload used to update the organization settings.
 */
@Serializable
data class AuditLogOrganizationUpdatedChangesRequested(
    /**
     * The organization title.
     */
    val title: String? = null,
    /**
     * The organization description.
     */
    val description: String? = null,
    /**
     * The organization name.
     */
    val name: String? = null,
    /**
     * Visibility of the threads page which shows messages created with the Assistants API and Playground.
     * One of `ANY_ROLE`, `OWNERS`, or `NONE`.
     */
    @SerialName("threads_ui_visibility")
    val threadsUiVisibility: String? = null,
    /**
     * Visibility of the usage dashboard which shows activity and costs for your organization. One of
     * `ANY_ROLE` or `OWNERS`.
     */
    @SerialName("usage_dashboard_visibility")
    val usageDashboardVisibility: String? = null,
    /**
     * How your organization logs data from supported API calls. One of `disabled`, `enabled_per_call`,
     * `enabled_for_all_projects`, or `enabled_for_selected_projects`
     */
    @SerialName("api_call_logging")
    val apiCallLogging: String? = null,
    /**
     * The list of project ids if api_call_logging is set to `enabled_for_selected_projects`
     */
    @SerialName("api_call_logging_project_ids")
    val apiCallLoggingProjectIds: String? = null
)
