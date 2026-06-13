// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

@Serializable
data class InputMessageResource(
    /**
     * The type of the message input. Always set to `message`.
     */
    val type: String? = null,
    /**
     * The role of the message input. One of `user`, `system`, or `developer`.
     */
    val role: String,
    /**
     * The status of item. One of `in_progress`, `completed`, or `incomplete`. Populated when items are
     * returned via API.
     */
    val status: String? = null,
    val content: site.addzero.api.openai.models.InputMessageContentList,
    /**
     * The unique ID of the message input.
     */
    val id: String
)
