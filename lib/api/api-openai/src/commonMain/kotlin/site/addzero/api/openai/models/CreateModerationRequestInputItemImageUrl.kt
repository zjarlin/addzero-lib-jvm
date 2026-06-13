// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Contains either an image URL or a data URL for a base64 encoded image.
 */
@Serializable
data class CreateModerationRequestInputItemImageUrl(
    /**
     * Either a URL of the image or the base64 encoded image data.
     */
    val url: String
)
