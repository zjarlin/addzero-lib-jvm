// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Paginated list of groups that have access to a project.
 */
@Serializable
data class ProjectGroupListResource(
    /**
     * Always `list`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Project group memberships returned in the current page.
     */
    val data: List<site.addzero.api.openai.models.ProjectGroup>,
    /**
     * Whether additional project group memberships are available.
     */
    @SerialName("has_more")
    val hasMore: Boolean,
    /**
     * Cursor to fetch the next page of results, or `null` when there are no more results.
     */
    val next: String?
)
