// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkillReferenceParam(
    /**
     * References a skill created with the /v1/skills endpoint.
     */
    val type: String = "skill_reference",
    /**
     * The ID of the referenced skill.
     */
    @SerialName("skill_id")
    val skillId: String,
    /**
     * Optional skill version. Use a positive integer or 'latest'. Omit for default.
     */
    val version: String? = null
)
