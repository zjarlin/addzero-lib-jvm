package site.addzero.network.call.payment.spi

data class PaymentOrderQueryResult(
    val channel: PaymentChannel,
    val orderNo: String,
    val status: PaymentOrderStatus,
    val totalAmount: String? = null,
    val paidAmount: String? = null,
    val platformTransactionNo: String? = null,
    val buyerId: String? = null,
    val rawResponse: String? = null,
)
