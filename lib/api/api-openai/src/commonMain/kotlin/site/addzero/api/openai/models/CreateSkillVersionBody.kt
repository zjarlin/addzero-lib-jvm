// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Uploads a new immutable version of a skill.
 */
@Serializable
data class CreateSkillVersionBody(
    val files: JsonElement,
    /**
     * Whether to set this version as the default.
     */
    val default: Boolean? = null
)
