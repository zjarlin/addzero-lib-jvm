package site.addzero.util

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Curl执行器，用于将解析后的Curl对象转换为实际的网络请求
 */
object CurlExecutor {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 执行解析后的Curl请求
     *
     * @param parsedCurl 解析后的Curl对象
     * @return 响应结果
     * @throws IOException 网络请求异常
     */
    fun execute(parsedCurl: ParsedCurl): Response {
        val request = buildRequest(parsedCurl)
        return client.newCall(request).execute()
    }



    fun execute(curl:String): Response {
        val parseCurl = CurlParser.parseCurl(curl)
        val execute = execute(parseCurl)
        return execute
    }


    /**
     * 异步执行解析后的Curl请求
     *
     * @param parsedCurl 解析后的Curl对象
     * @param callback 回调函数
     */
    fun executeAsync(parsedCurl: ParsedCurl, callback: Callback) {
        val request = buildRequest(parsedCurl)
        client.newCall(request).enqueue(callback)
    }

    /**
     * 根据解析后的Curl对象构建OkHttp请求
     *
     * @param parsedCurl 解析后的Curl对象
     * @return OkHttp请求对象
     */
    private fun buildRequest(parsedCurl: ParsedCurl): Request {
        val url = parsedCurl.url ?: throw IllegalArgumentException("URL不能为空")

        val requestBuilder = Request.Builder().url(url)

        // 设置请求方法
        val method = parsedCurl.method ?: "GET"

        // 添加请求头
        parsedCurl.headers?.forEach { (key, value) ->
            if (key != null && value != null) {
                requestBuilder.addHeader(key, value.toString())
            }
        }

        // 根据方法类型设置请求体
        when (method.uppercase()) {
            "GET", "HEAD" -> {
                // GET和HEAD请求通常没有请求体
                requestBuilder.method(method, null)
            }

            "POST", "PUT", "PATCH", "DELETE" -> {
                val body = createRequestBody(parsedCurl)
                requestBuilder.method(method, body)
            }

            else -> {
                // 默认使用GET方法
                requestBuilder.method("GET", null)
            }
        }

        return requestBuilder.build()
    }

    /**
     * 根据解析后的Curl对象创建请求体
     *
     * @param parsedCurl 解析后的Curl对象
     * @return 请求体对象
     */
    private fun createRequestBody(parsedCurl: ParsedCurl): RequestBody? {
        val bodyContent = parsedCurl.body ?: return null

        // 根据Content-Type确定媒体类型
        val contentType = parsedCurl.headers?.get("content-type")?.toString()
            ?: parsedCurl.contentType
            ?: "text/plain"

        return try {
            bodyContent.toRequestBody(contentType.toMediaType())
        } catch (e: Exception) {
            // 如果媒体类型无效，则使用默认的文本类型
            bodyContent.toRequestBody("text/plain".toMediaType())
        }
    }
}
