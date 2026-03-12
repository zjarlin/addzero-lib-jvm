package site.addzero.network.call.payment.alipay

import com.alipay.easysdk.factory.MultipleFactory
import com.alipay.easysdk.kernel.Config
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse
import site.addzero.network.call.payment.internal.PaymentConfigValue

internal interface AlipayPaymentClient {

    fun preCreate(orderName: String, orderNo: String, totalAmount: String): AlipayTradePrecreateResponse

    fun query(orderNo: String): AlipayTradeQueryResponse
}

internal class DefaultAlipayPaymentClient(
    config: AlipayPaymentConfig,
) : AlipayPaymentClient {

    private val factory: MultipleFactory = MultipleFactory().apply {
        setOptions(buildOptions(config))
    }

    override fun preCreate(
        orderName: String,
        orderNo: String,
        totalAmount: String,
    ): AlipayTradePrecreateResponse {
        return factory.FaceToFace().preCreate(orderName, orderNo, totalAmount)
    }

    override fun query(orderNo: String): AlipayTradeQueryResponse {
        return factory.Common().query(orderNo)
    }

    private fun buildOptions(config: AlipayPaymentConfig): Config {
        return Config().apply {
            protocol = config.protocol
            gatewayHost = config.gatewayHost
            appId = config.appId
            signType = config.signType
            merchantPrivateKey = PaymentConfigValue.readContent(config.merchantPrivateKey)
            alipayPublicKey = PaymentConfigValue.readContent(config.alipayPublicKey)
            notifyUrl = config.notifyUrl
            ignoreSSL = config.ignoreSsl
        }
    }
}
