// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Automatic thread title preferences for the session.
 */
@Serializable
data class ChatSessionAutomaticThreadTitling(
    /**
     * Whether automatic thread titling is enabled.
     */
    val enabled: Boolean
)
