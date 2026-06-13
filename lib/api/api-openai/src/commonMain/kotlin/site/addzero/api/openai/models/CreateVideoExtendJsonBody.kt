// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * JSON parameters for extending an existing generated video.
 */
@Serializable
data class CreateVideoExtendJsonBody(
    /**
     * Reference to the completed video to extend.
     */
    val video: site.addzero.api.openai.models.VideoReferenceInputParam,
    /**
     * Updated text prompt that directs the extension generation.
     */
    val prompt: String,
    /**
     * Length of the newly generated extension segment in seconds (allowed values: 4, 8, 12, 16, 20).
     */
    val seconds: site.addzero.api.openai.models.VideoSeconds
)
