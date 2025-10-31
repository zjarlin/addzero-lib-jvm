package site.addzero.util

import site.addzero.util.PayloadMutator.generateMutationRules
import site.addzero.util.PayloadMutator.mutatePayload
import java.util.*
import java.util.regex.Pattern


object CurlParserHelper {
    // Helper method to extract path params
    fun extractPathParams(url: String): MutableList<String?> {
        val UUID_PATTERN = Pattern.compile("^[a-f0-9\\-]{20,}$", Pattern.CASE_INSENSITIVE)
        val NUMERIC_ID_PATTERN = Pattern.compile("^\\d+$")

        // Only allow alphanumeric patterns with both letters and digits, at least 10 chars
        val ALPHANUMERIC_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z0-9_-]+$")
        val dynamicParams: MutableList<String?> = ArrayList<String?>()
        try {
            // Remove domain and query parameters
            var path = url.replace("https?://[^/]+".toRegex(), "")
            val queryIndex = path.indexOf('?')
            if (queryIndex != -1) {
                path = path.substring(0, queryIndex)
            }
            val segments = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (segment in segments) {
                if (segment.isEmpty()) continue
                if (segment.matches("^v\\d+$".toRegex())) continue
                // Match only if the segment looks like an ID or dynamic token
                if (UUID_PATTERN.matcher(segment).matches() ||
                    NUMERIC_ID_PATTERN.matcher(segment).matches() ||
                    ALPHANUMERIC_PATTERN.matcher(segment).matches()
                ) {
                    dynamicParams.add(segment)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dynamicParams
    }

    // Helper method to extract query parameters
    fun extractQueryParams(url: String): MutableMap<String?, Any?> {
        val queryParams: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        try {
            val queryIndex = url.indexOf('?')
            if (queryIndex == -1) {
                return queryParams // No query parameters
            }

            val queryString = url.substring(queryIndex + 1)
            val pairs = queryString.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (pair in pairs) {
                if (pair.contains("=")) {
                    val keyValue = pair.split("=".toRegex(), limit = 2).toTypedArray()
                    val key: String? = keyValue[0]
                    val value: String? = if (keyValue.size > 1) keyValue[1] else ""
                    queryParams.put(key, value)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return queryParams
    }

    // Helper method to modify existing query parameters
    fun modifyExistingQueryParams(url: String): String {
        try {
            val queryIndex = url.indexOf('?')
            if (queryIndex == -1) {
                return url // No query parameters to modify
            }

            val baseUrl = url.substring(0, queryIndex)
            val queryString = url.substring(queryIndex + 1)

            val queryParams: MutableMap<String?, String?> = HashMap<String?, String?>()
            val pairs = queryString.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (pair in pairs) {
                if (pair.contains("=")) {
                    val keyValue = pair.split("=".toRegex(), limit = 2).toTypedArray()
                    val key = keyValue[0]
                    val value: String? = if (keyValue.size > 1) keyValue[1] else ""

                    if (key.lowercase(Locale.getDefault()).contains("id") || key.lowercase(Locale.getDefault())
                            .contains("user")
                    ) {
                        queryParams.put(key, "invalid_id_123")
                    } else if (key.lowercase(Locale.getDefault()).contains("page") || key.lowercase(Locale.getDefault())
                            .contains("limit")
                    ) {
                        queryParams.put(key, "-1")
                    } else if (key.lowercase(Locale.getDefault())
                            .contains("status") || key.lowercase(Locale.getDefault()).contains("type")
                    ) {
                        queryParams.put(key, "invalid_status")
                    } else {
                        queryParams.put(key, "modified_" + value)
                    }
                }
            }

            val newQueryString = StringBuilder()
            for (entry in queryParams.entries) {
                if (newQueryString.length > 0) {
                    newQueryString.append("&")
                }
                newQueryString.append(entry.key).append("=").append(entry.value)
            }

            return baseUrl + "?" + newQueryString.toString()
        } catch (e: Exception) {
            return url + "&invalidParam=test"
        }
    }

    fun UpdatePayload(curlCommand: String): String? {
        val parsedCurl = ParsedCurl()
        // Extract body
        val bodyMatcher = Pattern.compile("--data(?:-raw)?\\s+'(\\{[\\s\\S]*?})'").matcher(curlCommand)
        var mutatedPayload: String? = null
        if (bodyMatcher.find()) {
            val body = bodyMatcher.group(1).replace("\\n", "").replace("\\", "")
            parsedCurl.body = body

            // Auto-generate mutation rules
            val mutationRules = generateMutationRules(body)

            // Mutate payload
            mutatedPayload = mutatePayload(body, mutationRules)
            parsedCurl.body = mutatedPayload
        }
        return mutatedPayload
    }
}
