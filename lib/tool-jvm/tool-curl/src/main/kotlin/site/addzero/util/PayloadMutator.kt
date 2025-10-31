package site.addzero.util

import com.google.gson.*;
object PayloadMutator {
    // Generate mutation rules based on value type
    fun generateMutationRules(payload: String?): MutableMap<String?, String?> {
        val rules: MutableMap<String?, String?> = HashMap<String?, String?>()
        try {
            val jsonElement: JsonElement = JsonParser.parseString(payload)
            collectMutationRules(jsonElement, rules)
        } catch (e: JsonSyntaxException) {
            System.err.println("⚠ Invalid JSON, cannot generate mutation rules.")
        }
        return rules
    }

    private fun collectMutationRules(element: JsonElement, rules: MutableMap<String?, String?>) {
        if (element.isJsonObject()) {
            for (entry in element.getAsJsonObject().entrySet()) {
                val key: String? = entry.key
                val value: JsonElement = entry.value
                if (value.isJsonPrimitive()) {
                    val primitive: JsonPrimitive = value.getAsJsonPrimitive()
                    if (primitive.isString()) rules.put(key, "number")
                    else if (primitive.isNumber()) rules.put(key, "string")
                    else rules.put(key, "null") // boolean or others
                } else {
                    collectMutationRules(value, rules)
                }
            }
        } else if (element.isJsonArray()) {
            for (item in element.getAsJsonArray()) {
                collectMutationRules(item, rules)
            }
        }
    }

    // Mutate payload values according to rules
    fun mutatePayload(payload: String?, mutationRules: MutableMap<String?, String?>): String? {
        try {
            val jsonElement: JsonElement = JsonParser.parseString(payload)
            mutateElement(jsonElement, mutationRules)
            return GsonBuilder().setPrettyPrinting().create().toJson(jsonElement)
        } catch (e: JsonSyntaxException) {
            System.err.println("⚠ Invalid JSON, cannot mutate payload.")
            return payload
        }
    }

    private fun mutateElement(element: JsonElement, mutationRules: MutableMap<String?, String?>) {
        if (element.isJsonObject()) {
            val obj: JsonObject = element.getAsJsonObject()
            for (entry in obj.entrySet()) {
                val key: String? = entry.key
                if (mutationRules.containsKey(key)) {
                    when (mutationRules.get(key)) {
                        "string" -> obj.addProperty(key, "mutated_string")
                        "number" -> obj.addProperty(key, 0)
                        "null" -> obj.add(key, JsonNull.INSTANCE)
                    }
                } else {
                    mutateElement(entry.value, mutationRules)
                }
            }
        } else if (element.isJsonArray()) {
            for (item in element.getAsJsonArray()) {
                mutateElement(item, mutationRules)
            }
        }
    }
}
