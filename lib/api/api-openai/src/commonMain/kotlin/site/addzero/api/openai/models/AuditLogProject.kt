// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * The project that the action was scoped to. Absent for actions not scoped to projects. Note that any
 * admin actions taken via Admin API keys are associated with the default project.
 */
@Serializable
data class AuditLogProject(
    /**
     * The project ID.
     */
    val id: String? = null,
    /**
     * The project title.
     */
    val name: String? = null
)
