package site.addzero.network.call.payment.alipay

import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AlipayPaymentProviderTest {

    @Test
    fun `createQrCode returns normalized qr code result`() {
        val client = FakeAlipayPaymentClient(
            preCreateResponse = AlipayTradePrecreateResponse().apply {
                code = "10000"
                msg = "Success"
                outTradeNo = "ALI-TEST-001"
                qrCode = "https://qr.example/alipay"
                httpBody = """{"qr_code":"ok"}"""
            },
            queryResponse = AlipayTradeQueryResponse(),
        )
        val provider = AlipayPaymentProvider(
            configSupplier = { testConfig() },
            clientFactory = { client },
            orderNumberGenerator = { "IGNORED" },
        )

        val result = provider.createQrCode("  支付测试订单  ", "12.30")

        assertEquals("支付测试订单", client.lastPreCreateOrderName)
        assertEquals("IGNORED", client.lastPreCreateOrderNo)
        assertEquals("12.30", client.lastPreCreateAmount)
        assertEquals("ALI-TEST-001", result.orderNo)
        assertEquals("支付测试订单", result.orderName)
        assertEquals("12.30", result.totalAmount)
        assertEquals("https://qr.example/alipay", result.qrCode)
        assertEquals("""{"qr_code":"ok"}""", result.rawResponse)
    }

    @Test
    fun `queryOrder maps alipay trade query result`() {
        val client = FakeAlipayPaymentClient(
            preCreateResponse = AlipayTradePrecreateResponse(),
            queryResponse = AlipayTradeQueryResponse().apply {
                code = "10000"
                msg = "Success"
                outTradeNo = "ALI-QUERY-001"
                tradeNo = "202603110001"
                tradeStatus = "TRADE_SUCCESS"
                totalAmount = "12.30"
                buyerPayAmount = "12.30"
                buyerUserId = "2088xxxx"
                httpBody = """{"trade_status":"TRADE_SUCCESS"}"""
            },
        )
        val provider = AlipayPaymentProvider(
            configSupplier = { testConfig() },
            clientFactory = { client },
            orderNumberGenerator = { "IGNORED" },
        )

        val result = provider.queryOrder(" ALI-QUERY-001 ")

        assertEquals("ALI-QUERY-001", client.lastQueryOrderNo)
        assertEquals("ALI-QUERY-001", result.orderNo)
        assertEquals("12.30", result.totalAmount)
        assertEquals("12.30", result.paidAmount)
        assertEquals("202603110001", result.platformTransactionNo)
        assertEquals("2088xxxx", result.buyerId)
        assertEquals("""{"trade_status":"TRADE_SUCCESS"}""", result.rawResponse)
        assertEquals(site.addzero.network.call.payment.spi.PaymentOrderStatus.SUCCESS, result.status)
    }

    private fun testConfig(): AlipayPaymentConfig {
        return AlipayPaymentConfig(
            appId = "test-app-id",
            merchantPrivateKey = "private-key",
            alipayPublicKey = "public-key",
            notifyUrl = "https://notify.example/alipay",
        )
    }

    private class FakeAlipayPaymentClient(
        private val preCreateResponse: AlipayTradePrecreateResponse,
        private val queryResponse: AlipayTradeQueryResponse,
    ) : AlipayPaymentClient {

        var lastPreCreateOrderName: String? = null
        var lastPreCreateOrderNo: String? = null
        var lastPreCreateAmount: String? = null
        var lastQueryOrderNo: String? = null

        override fun preCreate(
            orderName: String,
            orderNo: String,
            totalAmount: String,
        ): AlipayTradePrecreateResponse {
            lastPreCreateOrderName = orderName
            lastPreCreateOrderNo = orderNo
            lastPreCreateAmount = totalAmount
            return preCreateResponse
        }

        override fun query(orderNo: String): AlipayTradeQueryResponse {
            lastQueryOrderNo = orderNo
            return queryResponse
        }
    }
}
