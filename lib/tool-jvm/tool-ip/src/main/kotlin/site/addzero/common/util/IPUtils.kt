package site.addzero.common.util

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Locale

object IPUtils {

    private const val LOCALHOST = "127.0.0.1"

    private val virtualInterfacePrefixes = listOf(
        "lo",
        "utun",
        "awdl",
        "llw",
        "bridge",
        "docker",
        "veth",
        "vmnet",
        "br-",
        "tap",
        "tun",
        "gif",
        "stf",
    )

    private val preferredInterfacePrefixes = listOf("en", "eth", "wlan", "wl")

    @JvmStatic
    val localIp: String
        get() = localIpOrNull() ?: LOCALHOST

    @JvmStatic
    fun localIpOrNull(): String? {
        val networkInterfaces = networkInterfacesOrNull() ?: return null
        return networkInterfaces.asSequence()
            .filter { it.isUsableNetworkInterface() }
            .flatMap { it.localIpv4Candidates() }
            .maxByOrNull(LocalIpv4Candidate::score)
            ?.address
    }

    private fun networkInterfacesOrNull(): java.util.Enumeration<NetworkInterface>? {
        return try {
            NetworkInterface.getNetworkInterfaces()
        } catch (_: SocketException) {
            null
        }
    }

    private fun NetworkInterface.isUsableNetworkInterface(): Boolean {
        return try {
            if (!isUp || isLoopback || isPointToPoint) {
                return false
            }
            isRealNetworkInterfaceName(name)
        } catch (_: SocketException) {
            false
        }
    }

    private fun NetworkInterface.localIpv4Candidates(): Sequence<LocalIpv4Candidate> {
        return inetAddresses.asSequence()
            .filterIsInstance<Inet4Address>()
            .filter(::isUsableLocalIpv4)
            .map { inetAddress ->
                LocalIpv4Candidate(
                    address = inetAddress.hostAddress,
                    score = calculateIpv4Score(name, inetAddress),
                )
            }
    }

    internal fun isRealNetworkInterfaceName(interfaceName: String?): Boolean {
        val normalizedInterfaceName = interfaceName.orEmpty().lowercase(Locale.ROOT)
        return virtualInterfacePrefixes.none(normalizedInterfaceName::startsWith)
    }

    internal fun isUsableLocalIpv4(inetAddress: InetAddress): Boolean {
        if (inetAddress !is Inet4Address) {
            return false
        }
        if (inetAddress.isAnyLocalAddress || inetAddress.isLoopbackAddress
            || inetAddress.isLinkLocalAddress || inetAddress.isMulticastAddress
        ) {
            return false
        }
        return !isBenchmarkIpv4(inetAddress.address)
    }

    internal fun calculateIpv4Score(interfaceName: String?, inet4Address: Inet4Address): Int {
        var score = 0
        if (inet4Address.isSiteLocalAddress) {
            score += 100
        }
        if (isPreferredInterfaceName(interfaceName)) {
            score += 10
        }
        return score
    }

    private fun isPreferredInterfaceName(interfaceName: String?): Boolean {
        val normalizedInterfaceName = interfaceName.orEmpty().lowercase(Locale.ROOT)
        return preferredInterfacePrefixes.any(normalizedInterfaceName::startsWith)
    }

    private fun isBenchmarkIpv4(addressBytes: ByteArray): Boolean {
        val first = addressBytes[0].toInt() and 0xFF
        val second = addressBytes[1].toInt() and 0xFF
        return first == 198 && (second == 18 || second == 19)
    }

    private data class LocalIpv4Candidate(
        val address: String,
        val score: Int,
    )

}
