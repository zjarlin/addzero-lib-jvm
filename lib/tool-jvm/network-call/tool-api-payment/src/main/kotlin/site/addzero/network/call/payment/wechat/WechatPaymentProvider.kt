package site.addzero.network.call.payment.wechat

import com.wechat.pay.java.service.payments.model.Transaction
import com.wechat.pay.java.service.payments.nativepay.model.Amount
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest
import site.addzero.network.call.payment.internal.PaymentAmountUtils
import site.addzero.network.call.payment.internal.PaymentOrderNumberGenerator
import site.addzero.network.call.payment.internal.PaymentRequestValidator
import site.addzero.network.call.payment.spi.PaymentChannel
import site.addzero.network.call.payment.spi.PaymentOrderQueryResult
import site.addzero.network.call.payment.spi.PaymentOrderStatus
import site.addzero.network.call.payment.spi.PaymentProvider
import site.addzero.network.call.payment.spi.PaymentQrCodeResult

class WechatPaymentProvider internal constructor(
    private val configSupplier: () -> WechatPaymentConfig,
    private val clientFactory: (WechatPaymentConfig) -> WechatPaymentClient,
    private val orderNumberGenerator: (String) -> String,
) : PaymentProvider {

    constructor() : this(
        configSupplier = WechatPaymentConfig::fromEnvironment,
        clientFactory = ::DefaultWechatPaymentClient,
        orderNumberGenerator = PaymentOrderNumberGenerator::next,
    )

    constructor(config: WechatPaymentConfig) : this(
        configSupplier = { config },
        clientFactory = ::DefaultWechatPaymentClient,
        orderNumberGenerator = PaymentOrderNumberGenerator::next,
    )

    private val paymentConfig: WechatPaymentConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        configSupplier()
    }

    private val client: WechatPaymentClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        clientFactory(paymentConfig)
    }

    override val channel: PaymentChannel = PaymentChannel.WECHAT

    override fun createQrCode(orderName: String, totalAmount: String): PaymentQrCodeResult {
        val normalizedOrderName = PaymentRequestValidator.requireOrderName(orderName)
        val normalizedAmount = PaymentAmountUtils.normalizeYuan(totalAmount)
        val orderNo = orderNumberGenerator("WX")

        val amount = Amount().apply {
            total = PaymentAmountUtils.toFen(normalizedAmount)
            currency = paymentConfig.currency
        }
        val request = PrepayRequest().apply {
            appid = paymentConfig.appId
            mchid = paymentConfig.merchantId
            description = normalizedOrderName
            outTradeNo = orderNo
            notifyUrl = paymentConfig.notifyUrl
            this.amount = amount
        }

        val response = client.prepay(request)
        val qrCode = response.codeUrl?.trim()
        require(!qrCode.isNullOrEmpty()) {
            "Wechat native prepay succeeded but codeUrl is empty"
        }

        return PaymentQrCodeResult(
            channel = channel,
            orderNo = orderNo,
            orderName = normalizedOrderName,
            totalAmount = normalizedAmount,
            qrCode = qrCode,
            rawResponse = response.toString(),
        )
    }

    override fun queryOrder(orderNo: String): PaymentOrderQueryResult {
        val normalizedOrderNo = PaymentRequestValidator.requireOrderNo(orderNo)
        val request = QueryOrderByOutTradeNoRequest().apply {
            outTradeNo = normalizedOrderNo
            mchid = paymentConfig.merchantId
        }

        val response = client.queryOrderByOutTradeNo(request)
        return PaymentOrderQueryResult(
            channel = channel,
            orderNo = response.outTradeNo ?: normalizedOrderNo,
            status = mapTradeStatus(response),
            totalAmount = PaymentAmountUtils.fenToYuan(response.amount?.total),
            paidAmount = PaymentAmountUtils.fenToYuan(response.amount?.payerTotal),
            platformTransactionNo = response.transactionId,
            buyerId = response.payer?.openid,
            rawResponse = response.toString(),
        )
    }

    private fun mapTradeStatus(transaction: Transaction): PaymentOrderStatus {
        return when (transaction.tradeState) {
            Transaction.TradeStateEnum.SUCCESS -> PaymentOrderStatus.SUCCESS
            Transaction.TradeStateEnum.NOTPAY,
            Transaction.TradeStateEnum.USERPAYING,
            Transaction.TradeStateEnum.ACCEPT,
            -> PaymentOrderStatus.PENDING
            Transaction.TradeStateEnum.CLOSED -> PaymentOrderStatus.CLOSED
            Transaction.TradeStateEnum.PAYERROR,
            Transaction.TradeStateEnum.REVOKED,
            -> PaymentOrderStatus.FAILED
            Transaction.TradeStateEnum.REFUND -> PaymentOrderStatus.REFUNDED
            null -> PaymentOrderStatus.UNKNOWN
        }
    }
}
