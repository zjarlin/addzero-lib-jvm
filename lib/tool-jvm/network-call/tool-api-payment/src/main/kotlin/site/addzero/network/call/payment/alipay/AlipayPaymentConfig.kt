package site.addzero.network.call.payment.alipay

import site.addzero.network.call.payment.internal.PaymentConfigValue

data class AlipayPaymentConfig(
    val appId: String,
    val merchantPrivateKey: String,
    val alipayPublicKey: String,
    val notifyUrl: String,
    val gatewayHost: String = "openapi.alipay.com",
    val protocol: String = "https",
    val signType: String = "RSA2",
    val ignoreSsl: Boolean = false,
) {
    companion object {
        fun fromEnvironment(): AlipayPaymentConfig {
            return AlipayPaymentConfig(
                appId = PaymentConfigValue.required("ALIPAY_APP_ID"),
                merchantPrivateKey = PaymentConfigValue.required("ALIPAY_MERCHANT_PRIVATE_KEY"),
                alipayPublicKey = PaymentConfigValue.required("ALIPAY_PUBLIC_KEY"),
                notifyUrl = PaymentConfigValue.required("ALIPAY_NOTIFY_URL"),
                gatewayHost = PaymentConfigValue.optional("ALIPAY_GATEWAY_HOST", "openapi.alipay.com")
                    ?: "openapi.alipay.com",
                protocol = PaymentConfigValue.optional("ALIPAY_PROTOCOL", "https") ?: "https",
                signType = PaymentConfigValue.optional("ALIPAY_SIGN_TYPE", "RSA2") ?: "RSA2",
                ignoreSsl = PaymentConfigValue.optional("ALIPAY_IGNORE_SSL", "false")
                    ?.toBooleanStrictOrNull()
                    ?: false,
            )
        }
    }
}
