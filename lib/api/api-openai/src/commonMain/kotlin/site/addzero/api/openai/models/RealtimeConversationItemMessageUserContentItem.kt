// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeConversationItemMessageUserContentItem(
    /**
     * The content type (`input_text`, `input_audio`, or `input_image`).
     */
    val type: String? = null,
    /**
     * The text content (for `input_text`).
     */
    val text: String? = null,
    /**
     * Base64-encoded audio bytes (for `input_audio`), these will be parsed as the format specified in the
     * session input audio type configuration. This defaults to PCM 16-bit 24kHz mono if not specified.
     */
    val audio: String? = null,
    /**
     * Base64-encoded image bytes (for `input_image`) as a data URI. For example
     * `data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...`. Supported formats are PNG and JPEG.
     */
    @SerialName("image_url")
    val imageUrl: String? = null,
    /**
     * The detail level of the image (for `input_image`). `auto` will default to `high`.
     */
    val detail: String? = "auto",
    /**
     * Transcript of the audio (for `input_audio`). This is not sent to the model, but will be attached to
     * the message item for reference.
     */
    val transcript: String? = null
)
