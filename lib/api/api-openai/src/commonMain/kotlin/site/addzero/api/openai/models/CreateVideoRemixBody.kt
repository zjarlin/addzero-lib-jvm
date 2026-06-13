// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Parameters for remixing an existing generated video.
 */
@Serializable
data class CreateVideoRemixBody(
    /**
     * Updated text prompt that directs the remix generation.
     */
    val prompt: String
)
