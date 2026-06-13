// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Realtime MCP protocol error
 */
@Serializable
data class RealtimeMCPProtocolError(
    val type: String,
    val code: Int,
    val message: String
)
