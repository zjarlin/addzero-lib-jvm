// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * JSON parameters for editing an existing generated video.
 */
@Serializable
data class CreateVideoEditJsonBody(
    /**
     * Reference to the completed video to edit.
     */
    val video: site.addzero.api.openai.models.VideoReferenceInputParam,
    /**
     * Text prompt that describes how to edit the source video.
     */
    val prompt: String
)
