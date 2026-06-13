// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Uploads a skill either as a directory (multipart `files[]`) or as a single zip file.
 */
@Serializable
data class CreateSkillBody(
    val files: JsonElement
)
