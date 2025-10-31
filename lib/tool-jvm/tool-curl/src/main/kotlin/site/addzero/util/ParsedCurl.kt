package site.addzero.util

class ParsedCurl {
    // Getters and Setters
    var method: String? = null
    var url: String? = null
    var headers: MutableMap<String?, Any?>? = null
    var authorization: String? = null
    var body: String? = null
    var queryParams: MutableMap<String?, Any?>? = null
    var pathParams: MutableList<String?>? = null
    var formParams: MutableMap<String?, String?>? = null // Optional extension
    var contentType: String? = null // Optional extension
}
