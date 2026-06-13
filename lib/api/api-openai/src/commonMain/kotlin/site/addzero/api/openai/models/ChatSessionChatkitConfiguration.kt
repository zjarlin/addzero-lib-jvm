// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ChatKit configuration for the session.
 */
@Serializable
data class ChatSessionChatkitConfiguration(
    /**
     * Automatic thread titling preferences.
     */
    @SerialName("automatic_thread_titling")
    val automaticThreadTitling: site.addzero.api.openai.models.ChatSessionAutomaticThreadTitling,
    /**
     * Upload settings for the session.
     */
    @SerialName("file_upload")
    val fileUpload: site.addzero.api.openai.models.ChatSessionFileUpload,
    /**
     * History retention configuration.
     */
    val history: site.addzero.api.openai.models.ChatSessionHistory
)
