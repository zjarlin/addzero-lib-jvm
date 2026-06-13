// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Nested user details.
 */
@Serializable
data class UserUser(
    @SerialName("object")
    val objectType: String,
    val id: String,
    val email: String? = null,
    val name: String? = null,
    val picture: String? = null,
    val enabled: Boolean? = null,
    val banned: Boolean? = null,
    @SerialName("banned_at")
    val bannedAt: Long? = null
)
