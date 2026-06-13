// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Occurs when a new [thread](/docs/api-reference/threads/object) is created.
 */
@Serializable
data class ThreadStreamEvent2(
    /**
     * Whether to enable input audio transcription.
     */
    val enabled: Boolean? = null,
    val event: String,
    val data: site.addzero.api.openai.models.ThreadObject
)
