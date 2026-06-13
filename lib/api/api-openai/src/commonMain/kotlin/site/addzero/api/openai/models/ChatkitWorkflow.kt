// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Workflow metadata and state returned for the session.
 */
@Serializable
data class ChatkitWorkflow(
    /**
     * Identifier of the workflow backing the session.
     */
    val id: String,
    val version: String?,
    @SerialName("state_variables")
    val stateVariables: Map<String, JsonElement>?,
    /**
     * Tracing settings applied to the workflow.
     */
    val tracing: site.addzero.api.openai.models.ChatkitWorkflowTracing
)
