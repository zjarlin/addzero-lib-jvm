// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Paginated list of roles assigned to a principal.
 */
@Serializable
data class RoleListResource(
    /**
     * Always `list`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Role assignments returned in the current page.
     */
    val data: List<site.addzero.api.openai.models.AssignedRoleDetails>,
    /**
     * Whether additional assignments are available when paginating.
     */
    @SerialName("has_more")
    val hasMore: Boolean,
    /**
     * Cursor to fetch the next page of results, or `null` when there are no more assignments.
     */
    val next: String?
)
