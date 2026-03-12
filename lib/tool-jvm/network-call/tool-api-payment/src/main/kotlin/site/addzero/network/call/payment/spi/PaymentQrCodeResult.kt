package site.addzero.network.call.payment.spi

data class PaymentQrCodeResult(
    val channel: PaymentChannel,
    val orderNo: String,
    val orderName: String,
    val totalAmount: String,
    val qrCode: String,
    val rawResponse: String? = null,
)
