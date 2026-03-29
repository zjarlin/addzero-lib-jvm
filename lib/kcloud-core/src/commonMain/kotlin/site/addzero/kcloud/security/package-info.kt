/**
 * 安全加密层 - 端到端加密实现
 *
 * ## 职责
 * - 文件加密/解密
 * - 密钥管理
 * - 加密存储包装
 *
 * ## 加密方案
 * - 算法: AES-256-GCM (认证加密)
 * - 密钥派生: PBKDF2WithHmacSHA256 (100000 次迭代)
 * - 盐长度: 32 字节
 * - IV 长度: 12 字节 (GCM 推荐)
 * - 标签长度: 128 位
 *
 * ## 加密文件格式
 * ```
 * [Magic(4)] [Version(1)] [Salt(32)] [IV(12)] [FileNameLen(4)] [EncryptedFileName(?)] [EncryptedData]
 * ```
 *
 * ## 核心组件
 * - `EncryptionManager`（jvmMain）- 加密/解密操作
 * - [KeyStoreManager] - 密钥安全存储 (expect/actual)
 * - `EncryptedStorageClient`（jvmMain）- 透明加密存储包装器
 * - `EncryptionHeader`（jvmMain）- 加密文件头部信息
 *
 * ## 使用示例
 * ### 加密文件
 * ```kotlin
 * val encryptionManager = EncryptionManager()
 * encryptionManager.encryptFile(
 *     inputFile = File("document.pdf"),
 *     outputFile = File("document.pdf.enc"),
 *     password = "user_password"
 * )
 * ```
 *
 * ### 透明加密存储
 * ```kotlin
 * val s3Client = S3StorageClient(config)
 * val encryptedClient = EncryptedStorageClient(
 *     delegate = s3Client,
 *     encryptionManager = EncryptionManager(),
 *     passwordProvider = { getUserPassword() }
 * )
 * ```
 *
 * ## 安全注意事项
 * 1. 密码应通过安全方式获取（密码框等）
 * 2. 密钥不应硬编码在代码中
 * 3. 加密文件应添加 .enc 后缀以区分
 * 4. 密钥库文件应设置严格的文件权限
 *
 * @author zjarlin
 * @since 1.0.0
 */
package site.addzero.kcloud.security
