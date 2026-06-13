// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Optional per-session configuration settings for ChatKit behavior.
 */
@Serializable
data class ChatkitConfigurationParam(
    /**
     * Configuration for automatic thread titling. When omitted, automatic thread titling is enabled by
     * default.
     */
    @SerialName("automatic_thread_titling")
    val automaticThreadTitling: site.addzero.api.openai.models.AutomaticThreadTitlingParam? = null,
    /**
     * Configuration for upload enablement and limits. When omitted, uploads are disabled by default
     * (max_files 10, max_file_size 512 MB).
     */
    @SerialName("file_upload")
    val fileUpload: site.addzero.api.openai.models.FileUploadParam? = null,
    /**
     * Configuration for chat history retention. When omitted, history is enabled by default with no limit
     * on recent_threads (null).
     */
    val history: site.addzero.api.openai.models.HistoryParam? = null
)
