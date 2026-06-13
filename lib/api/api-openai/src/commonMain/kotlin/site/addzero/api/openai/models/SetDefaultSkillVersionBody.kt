// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Updates the default version pointer for a skill.
 */
@Serializable
data class SetDefaultSkillVersionBody(
    /**
     * The skill version number to set as default.
     */
    @SerialName("default_version")
    val defaultVersion: String
)
