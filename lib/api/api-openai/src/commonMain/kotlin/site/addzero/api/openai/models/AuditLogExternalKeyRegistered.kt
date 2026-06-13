// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The details for events with this `type`.
 */
@Serializable
data class AuditLogExternalKeyRegistered(
    /**
     * The ID of the external key configuration.
     */
    val id: String? = null,
    /**
     * The configuration for the external key.
     */
    val data: Map<String, JsonElement>? = null
)
