package site.addzero.network.call.payment.wechat

import site.addzero.network.call.payment.internal.PaymentConfigValue

data class WechatPaymentConfig(
    val appId: String,
    val merchantId: String,
    val merchantSerialNumber: String,
    val privateKey: String,
    val apiV3Key: String,
    val notifyUrl: String,
    val currency: String = "CNY",
) {
    companion object {
        fun fromEnvironment(): WechatPaymentConfig {
            return WechatPaymentConfig(
                appId = PaymentConfigValue.required("WECHAT_PAY_APP_ID"),
                merchantId = PaymentConfigValue.required("WECHAT_PAY_MERCHANT_ID"),
                merchantSerialNumber = PaymentConfigValue.required("WECHAT_PAY_MERCHANT_SERIAL_NUMBER"),
                privateKey = PaymentConfigValue.required("WECHAT_PAY_PRIVATE_KEY"),
                apiV3Key = PaymentConfigValue.required("WECHAT_PAY_API_V3_KEY"),
                notifyUrl = PaymentConfigValue.required("WECHAT_PAY_NOTIFY_URL"),
                currency = PaymentConfigValue.optional("WECHAT_PAY_CURRENCY", "CNY") ?: "CNY",
            )
        }
    }
}
