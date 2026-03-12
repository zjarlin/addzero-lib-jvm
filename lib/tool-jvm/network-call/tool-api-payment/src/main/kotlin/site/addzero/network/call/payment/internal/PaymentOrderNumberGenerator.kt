package site.addzero.network.call.payment.internal

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom

internal object PaymentOrderNumberGenerator {

    private val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    fun next(prefix: String): String {
        val timestamp = LocalDateTime.now().format(formatter)
        val randomSuffix = ThreadLocalRandom.current().nextInt(100_000, 1_000_000)
        return prefix.uppercase() + timestamp + randomSuffix
    }
}
