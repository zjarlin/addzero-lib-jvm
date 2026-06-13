// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRoleUpdateRequest(
    /**
     * `owner` or `reader`
     */
    val role: String? = null,
    /**
     * Role ID to assign to the user.
     */
    @SerialName("role_id")
    val roleId: String? = null,
    /**
     * Technical level metadata.
     */
    @SerialName("technical_level")
    val technicalLevel: String? = null,
    /**
     * Developer persona metadata.
     */
    @SerialName("developer_persona")
    val developerPersona: String? = null
)
