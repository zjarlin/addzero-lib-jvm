// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Occurs when parts of a [Message](/docs/api-reference/messages/object) are being streamed.
 */
@Serializable
data class MessageStreamEvent4(
    val event: String,
    val data: site.addzero.api.openai.models.MessageDeltaObject
)
