// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Confirmation payload returned after removing a group from a project.
 */
@Serializable
data class ProjectGroupDeletedResource(
    /**
     * Always `project.group.deleted`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Whether the group membership in the project was removed.
     */
    val deleted: Boolean
)
