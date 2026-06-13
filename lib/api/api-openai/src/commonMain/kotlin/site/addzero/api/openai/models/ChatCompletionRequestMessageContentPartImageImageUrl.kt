// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestMessageContentPartImageImageUrl(
    /**
     * Either a URL of the image or the base64 encoded image data.
     */
    val url: String,
    /**
     * Specifies the detail level of the image. Learn more in the [Vision guide](/docs/guides/vision#low-
     * or-high-fidelity-image-understanding).
     */
    val detail: String? = "auto"
)
