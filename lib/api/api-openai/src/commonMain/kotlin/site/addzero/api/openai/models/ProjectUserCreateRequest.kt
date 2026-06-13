// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectUserCreateRequest(
    /**
     * The ID of the user.
     */
    @SerialName("user_id")
    val userId: String? = null,
    /**
     * Email of the user to add.
     */
    val email: String? = null,
    /**
     * `owner` or `member`
     */
    val role: String
)
