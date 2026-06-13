// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Learn about [file inputs](/docs/guides/text) for text generation.
 */
@Serializable
data class ChatCompletionRequestMessageContentPartFile(
    /**
     * The type of the content part. Always `file`.
     */
    val type: String,
    val file: site.addzero.api.openai.models.ChatCompletionRequestMessageContentPartFileFile
)
