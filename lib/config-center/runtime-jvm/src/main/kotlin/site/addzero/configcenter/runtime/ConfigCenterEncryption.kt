package site.addzero.configcenter.runtime

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import site.addzero.configcenter.spec.ConfigEncryptionSpi

class EnvMasterKeyEncryptionSpi(
    private val bootstrap: ConfigCenterBootstrap,
) : ConfigEncryptionSpi {
    override fun encrypt(
        plainText: String,
    ): String {
        val keyBytes = resolvedKeyBytes()
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(keyBytes, "AES"),
            GCMParameterSpec(128, iv),
        )
        val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        val payload = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, payload, 0, iv.size)
        System.arraycopy(encrypted, 0, payload, iv.size, encrypted.size)
        return "v1:${Base64.getEncoder().encodeToString(payload)}"
    }

    override fun decrypt(
        cipherText: String,
    ): String {
        val payload = cipherText
            .removePrefix("v1:")
            .let { Base64.getDecoder().decode(it) }
        require(payload.size > 12) {
            "配置中心密文格式非法"
        }
        val iv = payload.copyOfRange(0, 12)
        val encrypted = payload.copyOfRange(12, payload.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(resolvedKeyBytes(), "AES"),
            GCMParameterSpec(128, iv),
        )
        return cipher.doFinal(encrypted).toString(StandardCharsets.UTF_8)
    }

    override fun canDecrypt(): Boolean {
        return !bootstrap.masterKey.isNullOrBlank()
    }

    private fun resolvedKeyBytes(): ByteArray {
        val masterKey = bootstrap.masterKey
            ?: error("缺少 CONFIG_CENTER_MASTER_KEY，无法处理加密配置")
        return MessageDigest.getInstance("SHA-256")
            .digest(masterKey.toByteArray(StandardCharsets.UTF_8))
            .copyOf(32)
    }
}

