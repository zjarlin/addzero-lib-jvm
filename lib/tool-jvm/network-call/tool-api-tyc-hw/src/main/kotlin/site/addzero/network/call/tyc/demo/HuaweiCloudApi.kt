/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2023. All rights reserved.
 */
package site.addzero.network.call.tyc.demo

import com.cloud.apigateway.sdk.utils.Client
import okhttp3.OkHttpClient
import com.cloud.apigateway.sdk.utils.Request
import site.addzero.network.call.tyc.util.Constant
import site.addzero.network.call.tyc.util.SSLCipherSuiteUtil

class HuaweiCloudApi(private val ak: String, private val sk: String) {

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

}
