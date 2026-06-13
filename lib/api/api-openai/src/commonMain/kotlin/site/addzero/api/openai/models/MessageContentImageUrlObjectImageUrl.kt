// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class MessageContentImageUrlObjectImageUrl(
    /**
     * The external URL of the image, must be a supported image types: jpeg, jpg, png, gif, webp.
     */
    val url: String,
    /**
     * Specifies the detail level of the image. `low` uses fewer tokens, you can opt in to high resolution
     * using `high`. Default value is `auto`
     */
    val detail: String? = "auto"
)
