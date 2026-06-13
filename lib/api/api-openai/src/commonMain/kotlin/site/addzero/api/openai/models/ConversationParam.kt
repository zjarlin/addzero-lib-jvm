// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The conversation that this response belongs to. Items from this conversation are prepended to
 * `input_items` for this response request. Input items and output items from this response are
 * automatically added to this conversation after this response completes.
 */
typealias ConversationParam = JsonElement
