// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Paginated list of roles available on an organization or project.
 */
@Serializable
data class PublicRoleListResource(
    /**
     * Always `list`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Roles returned in the current page.
     */
    val data: List<site.addzero.api.openai.models.Role>,
    /**
     * Whether more roles are available when paginating.
     */
    @SerialName("has_more")
    val hasMore: Boolean,
    /**
     * Cursor to fetch the next page of results, or `null` when there are no additional roles.
     */
    val next: String?
)
