// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Controls which (if any) tool is called by the model. `none` means the model will not call any tools
 * and instead generates a message. `auto` is the default value and means the model can pick between
 * generating a message or calling one or more tools. `required` means the model must call one or more
 * tools before responding to the user. Specifying a particular tool like `{"type": "file_search"}` or
 * `{"type": "function", "function": {"name": "my_function"}}` forces the model to call that tool.
 */
typealias AssistantsApiToolChoiceOption = JsonElement
