// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Granular configuration for tracing.
 */
@Serializable
data class RealtimeSessionTracing(
    /**
     * The name of the workflow to attach to this trace. This is used to name the trace in the traces
     * dashboard.
     */
    @SerialName("workflow_name")
    val workflowName: String? = null,
    /**
     * The group id to attach to this trace to enable filtering and grouping in the traces dashboard.
     */
    @SerialName("group_id")
    val groupId: String? = null,
    /**
     * The arbitrary metadata to attach to this trace to enable filtering in the traces dashboard.
     */
    val metadata: Map<String, JsonElement>? = null
)
