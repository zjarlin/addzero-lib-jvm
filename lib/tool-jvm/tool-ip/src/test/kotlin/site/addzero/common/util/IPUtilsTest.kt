package site.addzero.common.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.net.InetAddress

class IPUtilsTest {

    @Test
    fun `localIp should not be blank`() {
        assertTrue(IPUtils.localIp.isNotBlank())
    }

    @Test
    fun `private ipv4 should be usable`() {
        val inetAddress = InetAddress.getByName("192.168.31.75")

        assertTrue(IPUtils.isUsableLocalIpv4(inetAddress))
    }

    @Test
    fun `benchmark ipv4 should be skipped`() {
        val inetAddress = InetAddress.getByName("198.18.0.1")

        assertFalse(IPUtils.isUsableLocalIpv4(inetAddress))
    }

    @Test
    fun `tunnel interface should be skipped`() {
        assertFalse(IPUtils.isRealNetworkInterfaceName("utun1500"))
    }

    @Test
    fun `physical interface should be preferred`() {
        assertTrue(IPUtils.isRealNetworkInterfaceName("en0"))
    }

}
