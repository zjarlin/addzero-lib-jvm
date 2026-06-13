// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class LocalEnvironmentParam(
    /**
     * Use a local computer environment.
     */
    val type: String = "local",
    /**
     * An optional list of skills.
     */
    val skills: List<site.addzero.api.openai.models.LocalSkillParam>? = null
)
