// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The payload used to create the project.
 */
@Serializable
data class AuditLogProjectCreatedData(
    /**
     * The project name.
     */
    val name: String? = null,
    /**
     * The title of the project as seen on the dashboard.
     */
    val title: String? = null
)
