package site.addzero.network.call.musiclib.crypto

import java.math.BigInteger
import java.security.MessageDigest
import java.util.Base64

/**
 * 通用加密工具类
 */
object CommonCrypto {

    /**
     * MD5 哈希
     */
    fun md5(input: String): String {
        return MessageDigest.getInstance("MD5")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * MD5 哈希 (字节数组)
     */
    fun md5Bytes(input: ByteArray): ByteArray {
        return MessageDigest.getInstance("MD5").digest(input)
    }

    /**
     * SHA1 哈希
     */
    fun sha1(input: String): String {
        return MessageDigest.getInstance("SHA-1")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * Base64 编码
     */
    fun base64Encode(input: String): String {
        return Base64.getEncoder().encodeToString(input.toByteArray())
    }

    /**
     * Base64 解码
     */
    fun base64Decode(input: String): ByteArray {
        return Base64.getDecoder().decode(input)
    }

    /**
     * Base64 解码为字符串
     */
    fun base64DecodeToString(input: String): String {
        return String(Base64.getDecoder().decode(input))
    }

    /**
     * 字节数组转16进制字符串
     */
    fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    /**
     * 16进制字符串转字节数组
     */
    fun String.hexToBytes(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    /**
     * RSA 加密 (NoPadding)
     */
    fun rsaEncrypt(text: String, pubKey: String, modulus: String): String {
        val reversedText = text.reversed()
        val hexText = reversedText.toByteArray().toHex()

        val biText = BigInteger(hexText, 16)
        val biPub = BigInteger(pubKey, 16)
        val biMod = BigInteger(modulus, 16)

        val result = biText.modPow(biPub, biMod)
        return "%0256x".format(result)
    }
}
