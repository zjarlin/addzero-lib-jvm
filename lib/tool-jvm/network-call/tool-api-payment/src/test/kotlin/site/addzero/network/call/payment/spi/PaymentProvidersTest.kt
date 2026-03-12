package site.addzero.network.call.payment.spi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PaymentProvidersTest {

    @Test
    fun `service loader discovers default providers`() {
        val providers = PaymentProviders.reload()
        val channels = providers.map { it.channel }.toSet()

        assertTrue(PaymentChannel.ALIPAY in channels)
        assertTrue(PaymentChannel.WECHAT in channels)
    }

    @Test
    fun `load provider by string alias`() {
        val provider = PaymentProviders.load("wx")

        assertNotNull(provider)
        assertEquals(PaymentChannel.WECHAT, provider.channel)
    }
}
