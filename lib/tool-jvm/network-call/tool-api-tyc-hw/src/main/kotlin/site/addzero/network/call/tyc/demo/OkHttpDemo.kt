/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2023. All rights reserved.
 */
package site.addzero.network.call.tyc.demo

import com.huawei.apig.sdk.util.Constant
import com.sun.org.slf4j.internal.LoggerFactory
import com.sun.security.ntlm.Client
import okhttp3.OkHttpClient
import okhttp3.Request
import site.addzero.network.call.tyc.util.HostName
import site.addzero.network.call.tyc.util.SSLCipherSuiteUtil

object OkHttpDemo {
    private val LOGGER: Logger = LoggerFactory.getLogger(OkHttpDemo::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        // Create a new request.
        val OkHttpRequest: Request = Request()
        try {
            // 认证用的ak和sk硬编码到代码中或者明文存储都有很大的安全风险，建议在配置文件或者环境变量中密文存放，使用时解密，确保安全；
            // 本示例以ak和sk保存在环境变量中为例，运行本示例前请先在本地环境中设置环境变量HUAWEICLOUD_SDK_AK和HUAWEICLOUD_SDK_SK。
            OkHttpRequest.setKey(System.getenv("HUAWEICLOUD_SDK_AK"))
            OkHttpRequest.setSecret(System.getenv("HUAWEICLOUD_SDK_SK"))
            OkHttpRequest.setMethod("GET")
            OkHttpRequest.setUrl("your url")
            OkHttpRequest.addHeader("Content-Type", "text/plain")
            OkHttpRequest.setBody("demo")
        } catch (e: Exception) {
            LOGGER.error(e.message)
            return
        }
        try {
            // Sign the request.
            val signedRequest: Request = Client.signOkhttp(OkHttpRequest, Constant.SIGNATURE_ALGORITHM_SDK_HMAC_SHA256)
            val client: OkHttpClient
            if (Constant.DO_VERIFY) {
                // creat okhttpClient and verify ssl certificate
                HostName.setUrlHostName(OkHttpRequest.getHost())
                client = SSLCipherSuiteUtil.createOkHttpClientWithVerify(Constant.INTERNATIONAL_PROTOCOL)
            } else {
                // creat okhttpClient and do not verify ssl certificate
                client = SSLCipherSuiteUtil.createOkHttpClient(Constant.INTERNATIONAL_PROTOCOL)
            }
            // Send the request.
            val response = client.newCall(signedRequest).execute()
            // Print the status line of the response.
            LOGGER.info("status: " + response.code())
            // Print the body of the response.
            val resEntity = response.body()
        } catch (e: Exception) {
            LOGGER.error(e.message)
        }
    }
}
