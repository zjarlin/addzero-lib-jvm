// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Task entry that appears within a TaskGroup.
 */
@Serializable
data class TaskGroupTask(
    /**
     * Subtype for the grouped task.
     */
    val type: site.addzero.api.openai.models.TaskType,
    val heading: String?,
    val summary: String?
)
