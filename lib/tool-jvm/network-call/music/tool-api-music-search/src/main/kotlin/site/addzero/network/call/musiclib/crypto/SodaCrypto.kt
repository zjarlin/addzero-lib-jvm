package site.addzero.network.call.musiclib.crypto

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * 汽水音乐解密算法
 */
object SodaCrypto {

    /**
     * 解密汽水音乐音频
     * @param encrypted 加密数据
     * @param playAuth 播放授权码
     * @return 解密后的音频数据
     */
    fun decryptAudio(encrypted: ByteArray, playAuth: String): ByteArray {
        // 从 playAuth 提取密钥
        val key = extractKeyFromAuth(playAuth)

        // AES-128-ECB 解密
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))

        return cipher.doFinal(encrypted)
    }

    /**
     * 从播放授权码中提取密钥
     */
    private fun extractKeyFromAuth(playAuth: String): ByteArray {
        // 简单的密钥派生逻辑
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(playAuth.toByteArray())
        return hash.copyOfRange(0, 16)  // 取前16字节作为AES密钥
    }
}
