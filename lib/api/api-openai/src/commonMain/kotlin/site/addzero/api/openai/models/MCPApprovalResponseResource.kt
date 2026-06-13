// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A response to an MCP approval request.
 */
@Serializable
data class MCPApprovalResponseResource(
    /**
     * The type of the item. Always `mcp_approval_response`.
     */
    val type: String,
    /**
     * The unique ID of the approval response
     */
    val id: String,
    /**
     * The ID of the approval request being answered.
     */
    @SerialName("approval_request_id")
    val approvalRequestId: String,
    /**
     * Whether the request was approved.
     */
    val approve: Boolean,
    val reason: String? = null
)
