package com.addzero.core.network

import com.addzero.settings.SettingContext4Compose
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

object AddHttpClient {
    private var mytoken: String? = null

    fun setToken(token: String?) {
        this.mytoken = token
    }

    fun getToken(): String? {
        return mytoken
    }

    val httpclient by lazy {
        apiClient.config {
            configBaseUrl(SettingContext4Compose.BASE_URL)
//            configToken(mytoken)
            // 动态添加token到每个请求
            defaultRequest {
                headers {
                    mytoken?.let {
                        append(HttpHeaders.Authorization, it)
                    }
                }
            }
        }
    }

    val ktorfit = Ktorfit.Builder().httpClient(httpclient).build()
}
