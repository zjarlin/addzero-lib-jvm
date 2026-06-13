// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * On an incomplete message, details about why the message is incomplete.
 */
@Serializable
data class MessageObjectIncompleteDetails(
    /**
     * The reason the message is incomplete.
     */
    val reason: String
)
