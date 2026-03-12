package site.addzero.network.call.payment.spi

enum class PaymentChannel(
    val code: String,
    private vararg val aliases: String,
) {
    ALIPAY("alipay", "ali"),
    WECHAT("wechat", "wechatpay", "wx");

    fun matches(value: String): Boolean {
        val normalizedValue = value.trim().lowercase()
        if (normalizedValue == code) {
            return true
        }
        return aliases.any { it == normalizedValue }
    }

    companion object {
        fun fromValue(value: String): PaymentChannel {
            return entries.firstOrNull { it.matches(value) }
                ?: throw IllegalArgumentException("Unsupported payment channel: $value")
        }
    }
}
