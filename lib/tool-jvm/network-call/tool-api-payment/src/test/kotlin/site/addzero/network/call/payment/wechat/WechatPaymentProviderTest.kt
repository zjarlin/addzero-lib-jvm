package site.addzero.network.call.payment.wechat

import com.wechat.pay.java.service.payments.model.Transaction
import com.wechat.pay.java.service.payments.model.TransactionAmount
import com.wechat.pay.java.service.payments.model.TransactionPayer
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WechatPaymentProviderTest {

    @Test
    fun `createQrCode builds native pay request and returns qr code`() {
        val client = FakeWechatPaymentClient(
            prepayResponse = PrepayResponse().apply {
                codeUrl = "weixin://wxpay/bizpayurl?test=1"
            },
            transaction = Transaction(),
        )
        val provider = WechatPaymentProvider(
            configSupplier = { testConfig() },
            clientFactory = { client },
            orderNumberGenerator = { "WX-TEST-001" },
        )

        val result = provider.createQrCode("  微信支付订单  ", "8.88")

        val request = assertNotNull(client.lastPrepayRequest)
        assertEquals("test-app-id", request.appid)
        assertEquals("test-mch-id", request.mchid)
        assertEquals("微信支付订单", request.description)
        assertEquals("WX-TEST-001", request.outTradeNo)
        assertEquals("https://notify.example/wechat", request.notifyUrl)
        assertEquals(888, request.amount.total)
        assertEquals("CNY", request.amount.currency)
        assertEquals("WX-TEST-001", result.orderNo)
        assertEquals("微信支付订单", result.orderName)
        assertEquals("8.88", result.totalAmount)
        assertEquals("weixin://wxpay/bizpayurl?test=1", result.qrCode)
    }

    @Test
    fun `queryOrder maps wechat transaction result`() {
        val transaction = Transaction().apply {
            outTradeNo = "WX-QUERY-001"
            transactionId = "4200000001"
            tradeState = Transaction.TradeStateEnum.SUCCESS
            amount = TransactionAmount().apply {
                total = 888
                payerTotal = 888
                currency = "CNY"
            }
            payer = TransactionPayer().apply {
                openid = "openid-123"
            }
        }
        val client = FakeWechatPaymentClient(
            prepayResponse = PrepayResponse(),
            transaction = transaction,
        )
        val provider = WechatPaymentProvider(
            configSupplier = { testConfig() },
            clientFactory = { client },
            orderNumberGenerator = { "IGNORED" },
        )

        val result = provider.queryOrder(" WX-QUERY-001 ")

        val request = assertNotNull(client.lastQueryRequest)
        assertEquals("WX-QUERY-001", request.outTradeNo)
        assertEquals("test-mch-id", request.mchid)
        assertEquals("WX-QUERY-001", result.orderNo)
        assertEquals("8.88", result.totalAmount)
        assertEquals("8.88", result.paidAmount)
        assertEquals("4200000001", result.platformTransactionNo)
        assertEquals("openid-123", result.buyerId)
        assertEquals(site.addzero.network.call.payment.spi.PaymentOrderStatus.SUCCESS, result.status)
    }

    private fun testConfig(): WechatPaymentConfig {
        return WechatPaymentConfig(
            appId = "test-app-id",
            merchantId = "test-mch-id",
            merchantSerialNumber = "serial-no",
            privateKey = "private-key",
            apiV3Key = "api-v3-key",
            notifyUrl = "https://notify.example/wechat",
        )
    }

    private class FakeWechatPaymentClient(
        private val prepayResponse: PrepayResponse,
        private val transaction: Transaction,
    ) : WechatPaymentClient {

        var lastPrepayRequest: PrepayRequest? = null
        var lastQueryRequest: QueryOrderByOutTradeNoRequest? = null

        override fun prepay(request: PrepayRequest): PrepayResponse {
            lastPrepayRequest = request
            return prepayResponse
        }

        override fun queryOrderByOutTradeNo(request: QueryOrderByOutTradeNoRequest): Transaction {
            lastQueryRequest = request
            return transaction
        }
    }
}
