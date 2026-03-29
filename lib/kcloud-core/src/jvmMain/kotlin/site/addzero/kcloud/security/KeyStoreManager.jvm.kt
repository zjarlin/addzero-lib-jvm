package site.addzero.kcloud.security

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.Base64

actual class KeyStoreManager actual constructor() {
    private val keyDirectory: Path = Path.of(
        System.getProperty("user.home"),
        ".kcloud",
        "keystore",
    )

    actual fun storeKey(
        keyId: String,
        keyData: ByteArray,
    ): Boolean {
        return runCatching {
            Files.createDirectories(keyDirectory)
            Files.writeString(
                keyPath(keyId),
                Base64.getEncoder().encodeToString(keyData),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE,
            )
            true
        }.getOrDefault(false)
    }

    actual fun retrieveKey(
        keyId: String,
    ): ByteArray? {
        return runCatching {
            val path = keyPath(keyId)
            if (!Files.exists(path)) {
                return null
            }
            Base64.getDecoder().decode(Files.readString(path))
        }.getOrNull()
    }

    actual fun deleteKey(
        keyId: String,
    ): Boolean {
        return runCatching {
            Files.deleteIfExists(keyPath(keyId))
        }.getOrDefault(false)
    }

    actual fun hasKey(
        keyId: String,
    ): Boolean {
        return Files.exists(keyPath(keyId))
    }

    private fun keyPath(keyId: String): Path {
        val safeKeyId = keyId.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return keyDirectory.resolve("$safeKeyId.key")
    }
}
