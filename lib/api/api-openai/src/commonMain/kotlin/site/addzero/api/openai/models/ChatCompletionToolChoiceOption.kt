// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Controls which (if any) tool is called by the model. `none` means the model will not call any tool
 * and instead generates a message. `auto` means the model can pick between generating a message or
 * calling one or more tools. `required` means the model must call one or more tools. Specifying a
 * particular tool via `{"type": "function", "function": {"name": "my_function"}}` forces the model to
 * call that tool. `none` is the default when no tools are present. `auto` is the default if tools are
 * present.
 */
typealias ChatCompletionToolChoiceOption = JsonElement
