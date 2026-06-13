// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * A text content.
 */
@Serializable
data class TextContent(
    val type: String = "text",
    val text: String
)
