/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2023. All rights reserved.
 */
package site.addzero.network.call.tyc.demo

import com.cloud.apigateway.sdk.utils.Client
import okhttp3.OkHttpClient
import com.cloud.apigateway.sdk.utils.Request
import site.addzero.network.call.tyc.util.Constant
import site.addzero.network.call.tyc.util.SSLCipherSuiteUtil

class CompanySearchApi(private val ak: String, private val sk: String) {

    fun executeRequest(
        url: String,
        method: String = "GET",
        body: String? = null,
        headers: Map<String, String> = emptyMap()
    ): String? {
        // Create a new request.
        val OkHttpRequest: Request = Request()
        try {
            OkHttpRequest.setKey(ak)
            OkHttpRequest.setSecret(sk)
            OkHttpRequest.setMethod(method)
            OkHttpRequest.setUrl(url)

            // Add headers
            headers.forEach { (key, value) ->
                OkHttpRequest.addHeader(key, value)
            }

            // Set body if provided
            body?.let {
                OkHttpRequest.setBody(it)
            }
        } catch (e: Exception) {
            println("Error building request: ${e.message}")
            return null
        }

        try {
            // Sign the request.
            val signedRequest = Client.signOkhttp(OkHttpRequest, Constant.SIGNATURE_ALGORITHM_SDK_HMAC_SHA256)
            val client: OkHttpClient
            client = SSLCipherSuiteUtil.createOkHttpClient(Constant.INTERNATIONAL_PROTOCOL)
            // Send the request.
            val response = client.newCall(signedRequest).execute()
            // Print the status line of the response.
            println("status: " + response.code)
            // Return the body of the response.
            val string = response.body?.string()
            return string
        } catch (e: Exception) {
            println("Error executing request: ${e.message}")
            return null
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // 本示例以ak和sk保存在环境变量中为例，运行本示例前请先在本地环境中设置环境变量HUAWEICLOUD_SDK_AK和HUAWEICLOUD_SDK_SK。
            val ak = System.getenv("HUAWEICLOUD_SDK_AK") ?: throw IllegalStateException("HUAWEICLOUD_SDK_AK environment variable not set")
            val sk = System.getenv("HUAWEICLOUD_SDK_SK") ?: throw IllegalStateException("HUAWEICLOUD_SDK_SK environment variable not set")

            val demo = CompanySearchApi(ak, sk)
            val response = demo.executeRequest("your url", "GET", "demo", mapOf("Content-Type" to "text/plain"))
            response?.let { println("Response: $it") }
        }
    }
}
