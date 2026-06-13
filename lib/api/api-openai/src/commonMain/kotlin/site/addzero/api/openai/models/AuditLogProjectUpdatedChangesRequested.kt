// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The payload used to update the project.
 */
@Serializable
data class AuditLogProjectUpdatedChangesRequested(
    /**
     * The title of the project as seen on the dashboard.
     */
    val title: String? = null
)
