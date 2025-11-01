/**
 * Copyright 2019 bejson.com
 */
package site.addzero.network.call.tianyancha.domain.search



data class JsonRootBean(
    val state: String? = null,
    val message: String? = null,
    val special: String? = null,
    val vipMessage: String? = null,
    val isLogin: Int = 0,
    val data: Data? = null
)
