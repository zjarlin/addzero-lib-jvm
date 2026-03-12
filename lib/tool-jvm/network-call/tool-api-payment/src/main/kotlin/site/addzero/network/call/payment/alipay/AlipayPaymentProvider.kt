package site.addzero.network.call.payment.alipay

import site.addzero.network.call.payment.internal.PaymentAmountUtils
import site.addzero.network.call.payment.internal.PaymentOrderNumberGenerator
import site.addzero.network.call.payment.internal.PaymentRequestValidator
import site.addzero.network.call.payment.spi.PaymentChannel
import site.addzero.network.call.payment.spi.PaymentOrderQueryResult
import site.addzero.network.call.payment.spi.PaymentOrderStatus
import site.addzero.network.call.payment.spi.PaymentProvider
import site.addzero.network.call.payment.spi.PaymentQrCodeResult

class AlipayPaymentProvider internal constructor(
    private val configSupplier: () -> AlipayPaymentConfig,
    private val clientFactory: (AlipayPaymentConfig) -> AlipayPaymentClient,
    private val orderNumberGenerator: (String) -> String,
) : PaymentProvider {

    constructor() : this(
        configSupplier = AlipayPaymentConfig::fromEnvironment,
        clientFactory = ::DefaultAlipayPaymentClient,
        orderNumberGenerator = PaymentOrderNumberGenerator::next,
    )

    constructor(config: AlipayPaymentConfig) : this(
        configSupplier = { config },
        clientFactory = ::DefaultAlipayPaymentClient,
        orderNumberGenerator = PaymentOrderNumberGenerator::next,
    )

    private val paymentConfig: AlipayPaymentConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        configSupplier()
    }

    private val client: AlipayPaymentClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        clientFactory(paymentConfig)
    }

    override val channel: PaymentChannel = PaymentChannel.ALIPAY

    override fun createQrCode(orderName: String, totalAmount: String): PaymentQrCodeResult {
        val normalizedOrderName = PaymentRequestValidator.requireOrderName(orderName)
        val normalizedAmount = PaymentAmountUtils.normalizeYuan(totalAmount)
        val orderNo = orderNumberGenerator("ALI")

        val response = client.preCreate(normalizedOrderName, orderNo, normalizedAmount)
        ensureSuccess(
            code = response.code,
            message = response.msg,
            subCode = response.subCode,
            subMessage = response.subMsg,
        )

        val qrCode = response.qrCode?.trim()
        require(!qrCode.isNullOrEmpty()) {
            "Alipay preCreate succeeded but qrCode is empty"
        }

        return PaymentQrCodeResult(
            channel = channel,
            orderNo = response.outTradeNo ?: orderNo,
            orderName = normalizedOrderName,
            totalAmount = normalizedAmount,
            qrCode = qrCode,
            rawResponse = response.httpBody,
        )
    }

    override fun queryOrder(orderNo: String): PaymentOrderQueryResult {
        val normalizedOrderNo = PaymentRequestValidator.requireOrderNo(orderNo)
        val response = client.query(normalizedOrderNo)
        ensureSuccess(
            code = response.code,
            message = response.msg,
            subCode = response.subCode,
            subMessage = response.subMsg,
        )

        return PaymentOrderQueryResult(
            channel = channel,
            orderNo = response.outTradeNo ?: normalizedOrderNo,
            status = mapTradeStatus(response.tradeStatus),
            totalAmount = response.totalAmount,
            paidAmount = response.buyerPayAmount ?: response.receiptAmount,
            platformTransactionNo = response.tradeNo,
            buyerId = response.buyerUserId,
            rawResponse = response.httpBody,
        )
    }

    private fun ensureSuccess(
        code: String?,
        message: String?,
        subCode: String?,
        subMessage: String?,
    ) {
        if (code == "10000") {
            return
        }

        val errorMessage = buildString {
            append("Alipay request failed")
            if (!message.isNullOrBlank()) {
                append(": ")
                append(message)
            }
            if (!subCode.isNullOrBlank()) {
                append(" [")
                append(subCode)
                append("]")
            }
            if (!subMessage.isNullOrBlank()) {
                append(" ")
                append(subMessage)
            }
        }
        throw IllegalStateException(errorMessage)
    }

    private fun mapTradeStatus(tradeStatus: String?): PaymentOrderStatus {
        return when (tradeStatus) {
            "WAIT_BUYER_PAY" -> PaymentOrderStatus.PENDING
            "TRADE_SUCCESS", "TRADE_FINISHED" -> PaymentOrderStatus.SUCCESS
            "TRADE_CLOSED" -> PaymentOrderStatus.CLOSED
            else -> PaymentOrderStatus.UNKNOWN
        }
    }
}
