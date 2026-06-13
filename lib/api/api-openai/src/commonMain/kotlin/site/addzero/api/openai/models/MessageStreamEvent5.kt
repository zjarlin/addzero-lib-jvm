// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Occurs when a [message](/docs/api-reference/messages/object) is completed.
 */
@Serializable
data class MessageStreamEvent5(
    val event: String,
    val data: site.addzero.api.openai.models.MessageObject
)
