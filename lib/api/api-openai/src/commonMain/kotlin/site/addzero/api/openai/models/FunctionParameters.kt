// Generated from openai/openai-openapi openapi.yaml. Do not edit by hand.
package site.addzero.api.openai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The parameters the functions accepts, described as a JSON Schema object. See the
 * [guide](/docs/guides/function-calling) for examples, and the [JSON Schema reference](https://json-
 * schema.org/understanding-json-schema/) for documentation about the format. Omitting `parameters`
 * defines a function with an empty parameter list.
 */
typealias FunctionParameters = Map<String, JsonElement>
