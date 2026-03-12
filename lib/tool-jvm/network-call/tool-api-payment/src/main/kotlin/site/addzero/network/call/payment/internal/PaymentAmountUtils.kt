package site.addzero.network.call.payment.internal

import java.math.BigDecimal
import java.math.RoundingMode

internal object PaymentAmountUtils {

    fun normalizeYuan(totalAmount: String): String {
        return parseYuan(totalAmount).setScale(2, RoundingMode.UNNECESSARY).toPlainString()
    }

    fun toFen(totalAmount: String): Int {
        val fen = parseYuan(totalAmount)
            .movePointRight(2)
            .setScale(0, RoundingMode.UNNECESSARY)

        try {
            return fen.intValueExact()
        } catch (exception: ArithmeticException) {
            throw IllegalArgumentException("Payment amount is too large: $totalAmount", exception)
        }
    }

    fun fenToYuan(totalAmountInFen: Int?): String? {
        if (totalAmountInFen == null) {
            return null
        }
        return BigDecimal(totalAmountInFen)
            .movePointLeft(2)
            .setScale(2, RoundingMode.UNNECESSARY)
            .toPlainString()
    }

    private fun parseYuan(totalAmount: String): BigDecimal {
        val normalizedAmount = totalAmount.trim()
        require(normalizedAmount.isNotEmpty()) {
            "Payment amount must not be blank"
        }

        val amount = try {
            BigDecimal(normalizedAmount)
        } catch (exception: NumberFormatException) {
            throw IllegalArgumentException("Invalid payment amount: $totalAmount", exception)
        }

        require(amount > BigDecimal.ZERO) {
            "Payment amount must be greater than zero"
        }
        return amount
    }
}
