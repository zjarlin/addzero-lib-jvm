// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details about a group's membership in a project.
 */
@Serializable
data class ProjectGroup(
    /**
     * Always `project.group`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Identifier of the project.
     */
    @SerialName("project_id")
    val projectId: String,
    /**
     * Identifier of the group that has access to the project.
     */
    @SerialName("group_id")
    val groupId: String,
    /**
     * Display name of the group.
     */
    @SerialName("group_name")
    val groupName: String,
    /**
     * The type of the group.
     */
    @SerialName("group_type")
    val groupType: String,
    /**
     * Unix timestamp (in seconds) when the group was granted project access.
     */
    @SerialName("created_at")
    val createdAt: Long
)
