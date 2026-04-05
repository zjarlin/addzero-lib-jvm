/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */
package site.addzero.network.call.tyc.util

object Constant {
    // verify ssl certificate (true) or do not verify (false)
    const val DO_VERIFY = false

    const val HTTPS = "HTTPS"
    const val TRUST_MANAGER_FACTORY = "SunX509"
    const val GM_PROTOCOL = "GMTLS"
    const val INTERNATIONAL_PROTOCOL = "TLSv1.2"
    const val SIGNATURE_ALGORITHM_SDK_HMAC_SHA256 = "SDK-HMAC-SHA256"
    const val SIGNATURE_ALGORITHM_SDK_HMAC_SM3 = "SDK-HMAC-SM3"
    val SUPPORTED_CIPHER_SUITES = arrayOf<String?>(
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
    )
    const val SECURE_RANDOM_ALGORITHM_NATIVE_PRNG_NON_BLOCKING = "NativePRNGNonBlocking"
}
