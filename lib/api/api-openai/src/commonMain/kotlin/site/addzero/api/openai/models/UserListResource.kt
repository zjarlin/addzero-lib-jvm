// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Paginated list of user objects returned when inspecting group membership.
 */
@Serializable
data class UserListResource(
    /**
     * Always `list`.
     */
    @SerialName("object")
    val objectType: String,
    /**
     * Users in the current page.
     */
    val data: List<site.addzero.api.openai.models.GroupUser>,
    /**
     * Whether more users are available when paginating.
     */
    @SerialName("has_more")
    val hasMore: Boolean,
    /**
     * Cursor to fetch the next page of results, or `null` when no further users are available.
     */
    val next: String?
)
