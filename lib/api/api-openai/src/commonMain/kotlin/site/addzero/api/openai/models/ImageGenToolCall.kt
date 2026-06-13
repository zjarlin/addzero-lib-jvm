// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * An image generation request made by the model.
 */
@Serializable
data class ImageGenToolCall(
    /**
     * The type of the image generation call. Always `image_generation_call`.
     */
    val type: String,
    /**
     * The unique ID of the image generation call.
     */
    val id: String,
    /**
     * The status of the image generation call.
     */
    val status: String,
    val result: String?
)
