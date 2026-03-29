package site.addzero.system.spi

/**
 * 存储客户端接口 - 抽象S3/SSH等存储后端
 *
 * 定义所有存储操作的基本契约，支持上传、下载、删除、列出对象等操作。
 * 所有方法均为挂起函数，支持协程异步操作。
 *
 * ## 实现类
 * - [site.addzero.kcloud.storage.S3StorageClient] - S3 协议实现
 * - [site.addzero.kcloud.storage.SSHStorageClient] - SSH/SFTP 实现
 * - `FailoverStorageClient`（jvmMain）- 故障转移包装器
 *
 * @since 1.0.0
 */
interface StorageClient {

    /**
     * 列出指定前缀下的所有对象
     *
     * @param prefix 对象键前缀，null 表示根目录
     * @return 远程对象列表
     */
    suspend fun listObjects(prefix: String? = null): List<RemoteObject>

    /**
     * 上传本地文件到远程存储
     *
     * @param localPath 本地文件绝对路径
     * @param remotePath 远程存储路径
     * @param progress 进度回调 (已传输字节, 总字节)
     * @return 上传结果
     */
    suspend fun uploadObject(
        localPath: String,
        remotePath: String,
        progress: (Long, Long) -> Unit
    ): UploadResult

    /**
     * 下载远程对象到本地
     *
     * @param remotePath 远程存储路径
     * @param localPath 本地文件绝对路径
     * @param progress 进度回调 (已传输字节, 总字节)
     * @return 下载结果
     */
    suspend fun downloadObject(
        remotePath: String,
        localPath: String,
        progress: (Long, Long) -> Unit
    ): DownloadResult

    /**
     * 删除远程对象
     *
     * @param remotePath 远程存储路径
     * @return 是否删除成功
     */
    suspend fun deleteObject(remotePath: String): Boolean

    /**
     * 获取远程对象元数据
     *
     * @param remotePath 远程存储路径
     * @return 对象元数据，不存在时返回 null
     */
    suspend fun getObjectMetadata(remotePath: String): RemoteObject?

    /**
     * 测试存储连接是否可用
     *
     * @return 连接是否正常
     */
    suspend fun testConnection(): Boolean
}

/**
 * 远程对象元数据
 *
 * @param key 对象键（路径）
 * @param size 对象大小（字节）
 * @param etag 实体标签（用于检测变化）
 * @param versionId 版本ID（S3版本控制）
 * @param lastModified 最后修改时间戳
 */
data class RemoteObject(
    val key: String,
    val size: Long,
    val etag: String,
    val versionId: String? = null,
    val lastModified: Long
)

/**
 * 上传结果
 *
 * @param success 是否成功
 * @param etag 上传后的 ETag
 * @param versionId S3 版本ID
 * @param error 错误信息（失败时）
 */
data class UploadResult(
    val success: Boolean,
    val etag: String? = null,
    val versionId: String? = null,
    val error: String? = null
)

/**
 * 下载结果
 *
 * @param success 是否成功
 * @param bytesDownloaded 下载的字节数
 * @param error 错误信息（失败时）
 */
data class DownloadResult(
    val success: Boolean,
    val bytesDownloaded: Long = 0,
    val error: String? = null
)
