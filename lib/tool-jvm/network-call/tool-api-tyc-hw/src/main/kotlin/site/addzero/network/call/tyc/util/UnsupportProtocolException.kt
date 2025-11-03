/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */
package site.addzero.network.call.tyc.util

class UnsupportProtocolException(// 异常对应的描述信息
    private val msgDes: String?
) : Exception(msgDes) {
    companion object {
        private const val serialVersionUID = 4312820110480855928L
    }
}
