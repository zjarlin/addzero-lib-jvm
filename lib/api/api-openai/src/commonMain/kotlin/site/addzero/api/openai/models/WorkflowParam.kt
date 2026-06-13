// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Workflow reference and overrides applied to the chat session.
 */
@Serializable
data class WorkflowParam(
    /**
     * Identifier for the workflow invoked by the session.
     */
    val id: String,
    /**
     * Specific workflow version to run. Defaults to the latest deployed version.
     */
    val version: String? = null,
    /**
     * State variables forwarded to the workflow. Keys may be up to 64 characters, values must be primitive
     * types, and the map defaults to an empty object.
     */
    @SerialName("state_variables")
    val stateVariables: Map<String, JsonElement>? = null,
    /**
     * Optional tracing overrides for the workflow invocation. When omitted, tracing is enabled by default.
     */
    val tracing: site.addzero.api.openai.models.WorkflowTracingParam? = null
)
