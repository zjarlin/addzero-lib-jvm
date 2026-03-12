package site.addzero.network.call.payment.internal

internal object PaymentRequestValidator {

    fun requireOrderName(orderName: String): String {
        val normalizedValue = orderName.trim()
        require(normalizedValue.isNotEmpty()) {
            "Order name must not be blank"
        }
        return normalizedValue
    }

    fun requireOrderNo(orderNo: String): String {
        val normalizedValue = orderNo.trim()
        require(normalizedValue.isNotEmpty()) {
            "Order number must not be blank"
        }
        return normalizedValue
    }
}
