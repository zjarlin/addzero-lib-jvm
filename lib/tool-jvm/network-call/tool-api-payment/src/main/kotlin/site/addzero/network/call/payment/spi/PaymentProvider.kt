package site.addzero.network.call.payment.spi

/**
 * 支付渠道 SPI
 *
 * 通过 Java ServiceLoader 自动发现实现类。
 */
interface PaymentProvider {

    val channel: PaymentChannel

    /**
     * 生成支付二维码
     *
     * @param orderName 订单名称
     * @param totalAmount 订单金额，单位元，例如 `12.34`
     */
    fun createQrCode(orderName: String, totalAmount: String): PaymentQrCodeResult

    /**
     * 查询订单
     *
     * @param orderNo 商户订单号
     */
    fun queryOrder(orderNo: String): PaymentOrderQueryResult
}
