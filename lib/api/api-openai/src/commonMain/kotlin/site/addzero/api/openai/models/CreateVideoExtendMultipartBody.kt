// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Multipart parameters for extending an existing generated video.
 */
@Serializable
data class CreateVideoExtendMultipartBody(
    val video: JsonElement,
    /**
     * Updated text prompt that directs the extension generation.
     */
    val prompt: String,
    /**
     * Length of the newly generated extension segment in seconds (allowed values: 4, 8, 12, 16, 20).
     */
    val seconds: site.addzero.api.openai.models.VideoSeconds
)
