package site.addzero.network.call.payment.spi

enum class PaymentOrderStatus {
    PENDING,
    SUCCESS,
    CLOSED,
    FAILED,
    REFUNDED,
    UNKNOWN,
}
