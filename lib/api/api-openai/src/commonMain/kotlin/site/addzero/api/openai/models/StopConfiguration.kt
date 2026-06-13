// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Not supported with latest reasoning models `o3` and `o4-mini`. Up to 4 sequences where the API will
 * stop generating further tokens. The returned text will not contain the stop sequence.
 */
typealias StopConfiguration = JsonElement
