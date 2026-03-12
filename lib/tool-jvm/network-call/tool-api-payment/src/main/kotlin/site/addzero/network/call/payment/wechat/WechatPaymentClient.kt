package site.addzero.network.call.payment.wechat

import com.wechat.pay.java.core.Config
import com.wechat.pay.java.core.RSAAutoCertificateConfig
import com.wechat.pay.java.service.payments.model.Transaction
import com.wechat.pay.java.service.payments.nativepay.NativePayService
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest
import site.addzero.network.call.payment.internal.PaymentConfigValue

internal interface WechatPaymentClient {

    fun prepay(request: PrepayRequest): PrepayResponse

    fun queryOrderByOutTradeNo(request: QueryOrderByOutTradeNoRequest): Transaction
}

internal class DefaultWechatPaymentClient(
    config: WechatPaymentConfig,
) : WechatPaymentClient {

    private val nativePayService: NativePayService = NativePayService.Builder()
        .config(buildConfig(config))
        .build()

    override fun prepay(request: PrepayRequest): PrepayResponse {
        return nativePayService.prepay(request)
    }

    override fun queryOrderByOutTradeNo(request: QueryOrderByOutTradeNoRequest): Transaction {
        return nativePayService.queryOrderByOutTradeNo(request)
    }

    private fun buildConfig(config: WechatPaymentConfig): Config {
        return RSAAutoCertificateConfig.Builder()
            .merchantId(config.merchantId)
            .merchantSerialNumber(config.merchantSerialNumber)
            .privateKey(PaymentConfigValue.readContent(config.privateKey))
            .apiV3Key(config.apiV3Key)
            .build()
    }
}
