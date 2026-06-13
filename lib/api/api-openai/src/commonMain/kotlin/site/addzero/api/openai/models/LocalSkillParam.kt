// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class LocalSkillParam(
    /**
     * The name of the skill.
     */
    val name: String,
    /**
     * The description of the skill.
     */
    val description: String,
    /**
     * The path to the directory containing the skill.
     */
    val path: String
)
