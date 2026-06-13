// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class InlineSkillParam(
    /**
     * Defines an inline skill for this request.
     */
    val type: String = "inline",
    /**
     * The name of the skill.
     */
    val name: String,
    /**
     * The description of the skill.
     */
    val description: String,
    /**
     * Inline skill payload
     */
    val source: site.addzero.api.openai.models.InlineSkillSourceParam
)
