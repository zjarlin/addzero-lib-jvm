package site.addzero.util

import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Locale
import java.util.regex.Pattern


object CurlParser {
    fun parseCurl(curlCommand: String): ParsedCurl {
        val parsedCurl = ParsedCurl()

        // Normalize: join line continuations and collapse multiple spaces
        val normalized = curlCommand.replace("\\\\\\n".toRegex(), " ").replace("\\s+\\\\\\s*".toRegex(), " ")

        // Extract method (-X/--request) and other method hints (-I/--head)
        val methodMatcher = Pattern.compile("(?i)(?:--request|-X)\\s+(\\w+)").matcher(normalized)
        if (methodMatcher.find()) {
            parsedCurl.method = methodMatcher.group(1).uppercase(Locale.getDefault())
        }
        if (Pattern.compile("(?i)(?:--head|-I)\\b").matcher(normalized).find()) {
            parsedCurl.method = "HEAD"
        }

        // Infer method from body/form flags if not explicitly set
        if (parsedCurl.method == null) {
            if (Pattern.compile("(?i)(?:--data|-d|--data-raw|--data-binary|--data-urlencode|--form|-F)\\b")
                    .matcher(normalized).find()
            ) {
                parsedCurl.method = "POST"
            } else {
                parsedCurl.method = "GET"
            }
        }

        // Extract URL: support --url, quoted/unquoted http(s) tokens
        var url: String? = null
        val urlFlag = Pattern.compile("(?i)--url\\s+([^\\s]+)|--url\\s+(['\"])\\s*([^\"]*?)\\2")
            .matcher(normalized)
        if (urlFlag.find()) {
            url = if (urlFlag.group(1) != null) urlFlag.group(1) else urlFlag.group(3)
        }
        if (url == null) {
            // quoted http(s)
            val qHttp = Pattern.compile("(['\"])((?:https?://)[^'\"]+)\\1").matcher(normalized)
            if (qHttp.find()) url = qHttp.group(2)
        }
        if (url == null) {
            // unquoted http(s)
            val uqHttp = Pattern.compile("\\bhttps?://\\S+").matcher(normalized)
            if (uqHttp.find()) url = uqHttp.group()
        }
        parsedCurl.url = url

        // Extract headers: -H/--header with single or double quotes
        val headers: MutableMap<String?, Any?> = HashMap()
        val headerMatcher =
            Pattern.compile("(?i)(?:-H|--header)\\s+(['\"])\\s*([^:]+):\\s*([^\"]*?)\\1")
                .matcher(normalized)
        while (headerMatcher.find()) {
            val key = headerMatcher.group(2).trim { it <= ' ' }
            val value = headerMatcher.group(3).trim { it <= ' ' }
            headers[key] = value
        }
        // Also capture unquoted header values (best-effort)
        val headerMatcher2 = Pattern.compile("(?i)(?:-H|--header)\\s+([^'\"\n]+)").matcher(normalized)
        while (headerMatcher2.find()) {
            val hv = headerMatcher2.group(1).trim { it <= ' ' }
            val idx = hv.indexOf(":")
            if (idx > 0) {
                headers[hv.substring(0, idx).trim { it <= ' ' }] = hv.substring(idx + 1).trim { it <= ' ' }
            }
        }

        // Cookies: --cookie/ -b
        val cookieMatcher =
            Pattern.compile("(?i)(?:--cookie|-b)\\s+(['\"])\\s*([^\"]*?)\\1").matcher(normalized)
        if (cookieMatcher.find()) {
            headers["Cookie"] = cookieMatcher.group(2).trim { it <= ' ' }
        }

        // Basic auth: -u/--user user:pass
        val userMatcher =
            Pattern.compile("(?i)(?:-u|--user)\\s+(['\"])?.*?([A-Za-z0-9._%+-]+:[^'\"\\s]+)\\1?")
                .matcher(normalized)
        if (userMatcher.find()) {
            val cred = userMatcher.group(2)
            val encoded =
                Base64.getEncoder().encodeToString(cred.toByteArray(StandardCharsets.UTF_8))
            if (!headers.containsKey("Authorization")) {
                headers["Authorization"] = "Basic $encoded"
            }
        }

        parsedCurl.headers = headers

        // Extract form params: -F/--form field=value
        val formParams: MutableMap<String?, String?> = HashMap()
        val formMatcher =
            Pattern.compile("(?i)(?:-F|--form)\\s+(['\"])\\s*([^=]+)=([^\"]*?)\\1").matcher(normalized)
        while (formMatcher.find()) {
            formParams[formMatcher.group(2).trim { it <= ' ' }] = formMatcher.group(3).trim { it <= ' ' }
        }
        if (formParams.isNotEmpty()) {
            parsedCurl.formParams = formParams
            parsedCurl.contentType = "multipart/form-data"
            if (!headers.containsKey("Content-Type")) {
                headers["Content-Type"] = "multipart/form-data"
            }
        }

        // Extract body: --data/-d/--data-raw/--data-binary/--data-urlencode (single or double quotes)
        var body: String? = null
        val dataMatcher = Pattern.compile(
            "(?i)(?:--data|-d|--data-raw|--data-binary|--data-urlencode)\\s+(['\"])\\s*([\\r\\t\\s\\S]*?)\\1"
        ).matcher(normalized)
        if (dataMatcher.find()) {
            body = dataMatcher.group(2).trim { it <= ' ' }
        } else {
            // unquoted simple data
            val dataMatcher2 =
                Pattern.compile("(?i)(?:--data|-d)\\s+([^'\\\\\"\\s][^\\\\n]*)").matcher(normalized)
            if (dataMatcher2.find()) body = dataMatcher2.group(1).trim { it <= ' ' }
        }
        if (body != null) {
            parsedCurl.body = body.replace("\\n", "")
            // Infer JSON content type if body looks like JSON and not set
            if (!headers.containsKey("Content-Type") && body.trim { it <= ' ' }.startsWith("{")) {
                parsedCurl.contentType = "application/json"
                headers["Content-Type"] = "application/json"
            }
        }

        // Extract path parameters and query parameters from URL
        if (parsedCurl.url != null) {
            parsedCurl.pathParams = CurlParserHelper.extractPathParams(parsedCurl.url!!)
            parsedCurl.queryParams = CurlParserHelper.extractQueryParams(parsedCurl.url!!)
        }
        return parsedCurl
    }
}
