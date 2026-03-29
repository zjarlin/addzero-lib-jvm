package site.addzero.kcloud.security

/**
 * 密钥管理器 - 管理加密密钥的存储和检索
 *
 * 注意：密钥存储需要平台特定的安全存储实现（如 macOS Keychain、Windows DPAPI、Linux Keyring）
 */
expect class KeyStoreManager() {
    /**
     * 存储密钥
     */
    fun storeKey(keyId: String, keyData: ByteArray): Boolean

    /**
     * 检索密钥
     */
    fun retrieveKey(keyId: String): ByteArray?

    /**
     * 删除密钥
     */
    fun deleteKey(keyId: String): Boolean

    /**
     * 检查密钥是否存在
     */
    fun hasKey(keyId: String): Boolean
}
