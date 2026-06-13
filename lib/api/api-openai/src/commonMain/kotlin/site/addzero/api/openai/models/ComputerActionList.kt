// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable

/**
 * Flattened batched actions for `computer_use`. Each action includes an `type` discriminator and
 * action-specific fields.
 */
typealias ComputerActionList = List<site.addzero.api.openai.models.ComputerAction>
