// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Parameters for editing an existing generated video.
 */
@Serializable
data class CreateVideoEditMultipartBody(
    val video: JsonElement,
    /**
     * Text prompt that describes how to edit the source video.
     */
    val prompt: String
)
