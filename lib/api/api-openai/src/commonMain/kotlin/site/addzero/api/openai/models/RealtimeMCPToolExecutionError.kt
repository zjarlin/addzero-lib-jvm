// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Realtime MCP tool execution error
 */
@Serializable
data class RealtimeMCPToolExecutionError(
    val type: String,
    val message: String
)
