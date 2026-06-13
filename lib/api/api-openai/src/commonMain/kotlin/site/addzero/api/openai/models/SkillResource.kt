// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkillResource(
    /**
     * Unique identifier for the skill.
     */
    val id: String,
    /**
     * The object type, which is `skill`.
     */
    @SerialName("object")
    val objectType: String = "skill",
    /**
     * Name of the skill.
     */
    val name: String,
    /**
     * Description of the skill.
     */
    val description: String,
    /**
     * Unix timestamp (seconds) for when the skill was created.
     */
    @SerialName("created_at")
    val createdAt: Long,
    /**
     * Default version for the skill.
     */
    @SerialName("default_version")
    val defaultVersion: String,
    /**
     * Latest version for the skill.
     */
    @SerialName("latest_version")
    val latestVersion: String
)
