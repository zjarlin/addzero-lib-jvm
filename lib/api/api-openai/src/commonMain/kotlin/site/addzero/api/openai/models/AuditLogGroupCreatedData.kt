// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Information about the created group.
 */
@Serializable
data class AuditLogGroupCreatedData(
    /**
     * The group name.
     */
    @SerialName("group_name")
    val groupName: String? = null
)
