// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeletedSkillResource(
    @SerialName("object")
    val objectType: String = "skill.deleted",
    val deleted: Boolean,
    val id: String
)
