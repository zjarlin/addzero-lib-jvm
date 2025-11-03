/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */
package site.addzero.network.call.tyc.util

object HostName {
    private var urlHostName: String? = null

    fun setUrlHostName(hostName: String) {
        urlHostName = hostName
    }

    fun checkHostName(SSLHostName: String?): Boolean {
        return urlHostName == SSLHostName
    }
}
