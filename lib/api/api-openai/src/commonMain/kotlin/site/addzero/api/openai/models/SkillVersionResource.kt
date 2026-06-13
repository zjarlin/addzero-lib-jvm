// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkillVersionResource(
    /**
     * The object type, which is `skill.version`.
     */
    @SerialName("object")
    val objectType: String = "skill.version",
    /**
     * Unique identifier for the skill version.
     */
    val id: String,
    /**
     * Identifier of the skill for this version.
     */
    @SerialName("skill_id")
    val skillId: String,
    /**
     * Version number for this skill.
     */
    val version: String,
    /**
     * Unix timestamp (seconds) for when the version was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * Name of the skill version.
     */
    val name: String,
    /**
     * Description of the skill version.
     */
    val description: String
)
