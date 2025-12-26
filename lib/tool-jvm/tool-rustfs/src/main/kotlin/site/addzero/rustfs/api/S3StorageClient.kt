package site.addzero.rustfs.api

import site.addzero.common.result.SimpleResult
import site.addzero.common.result.Result
import java.io.File
import java.nio.file.Path
import java.time.Instant

/**
 * S3 兼容存储客户端接口
 *
 * 此接口定义了对象存储的基本操作，支持 S3、MinIO、RustFS 等兼容 S3 协议的存储服务。
 * 实现此接口可以适配不同的存储后端。
 */
interface S3StorageClient {

    /**
     * 对象元数据
     */
    data class ObjectMetadata(
        val key: String,
        val size: Long,
        val etag: String?,
        val lastModified: Instant?,
        val contentType: String?,
        val metadata: Map<String, String> = emptyMap()
    )

    /**
     * 分片信息
     */
    data class PartInfo(
        val partNumber: Int,
        val size: Long,
        val etag: String?
    )

    /**
     * 预签名 URL 信息
     */
    data class PresignedUrl(
        val url: String,
        val expiration: Instant
    )

    // ==================== Bucket 操作 ====================

    /**
     * 检查存储桶是否存在
     */
    fun bucketExists(bucketName: String): Boolean

    /**
     * 创建存储桶
     */
    fun createBucket(bucketName: String): SimpleResult

    /**
     * 列出所有存储桶
     */
    fun listBuckets(): List<String>

    /**
     * 删除存储桶
     */
    fun deleteBucket(bucketName: String): SimpleResult

    // ==================== 对象操作 ====================

    /**
     * 检查对象是否存在
     */
    fun objectExists(bucketName: String, key: String): Boolean

    /**
     * 获取对象元数据
     */
    fun getObjectMetadata(bucketName: String, key: String): ObjectMetadata?

    /**
     * 上传对象（字节数组）
     */
    fun putObject(
        bucketName: String,
        key: String,
        data: ByteArray,
        contentType: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): SimpleResult

    /**
     * 上传对象（文件）
     */
    fun putObject(
        bucketName: String,
        key: String,
        file: File,
        contentType: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): SimpleResult

    /**
     * 上传对象（Path）
     */
    fun putObject(
        bucketName: String,
        key: String,
        path: Path,
        contentType: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): SimpleResult {
        return putObject(bucketName, key, path.toFile(), contentType, metadata)
    }

    /**
     * 获取对象内容
     */
    fun getObject(bucketName: String, key: String): ByteArray?

    /**
     * 获取对象到文件
     */
    fun getObject(bucketName: String, key: String, targetFile: File): Boolean {
        val data = getObject(bucketName, key) ?: return false
        return try {
            targetFile.writeBytes(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 删除对象
     */
    fun deleteObject(bucketName: String, key: String): SimpleResult

    /**
     * 批量删除对象
     */
    fun deleteObjects(bucketName: String, keys: List<String>): SimpleResult

    /**
     * 复制对象
     */
    fun copyObject(
        sourceBucket: String,
        sourceKey: String,
        targetBucket: String,
        targetKey: String
    ): SimpleResult

    /**
     * 列出对象
     */
    fun listObjects(
        bucketName: String,
        prefix: String? = null,
        recursive: Boolean = true,
        maxKeys: Int = 1000
    ): List<ObjectMetadata>

    // ==================== 分片上传操作 ====================

    /**
     * 初始化分片上传
     */
    fun initMultipartUpload(
        bucketName: String,
        key: String,
        contentType: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Result<String>

    /**
     * 上传分片
     */
    fun uploadPart(
        bucketName: String,
        key: String,
        uploadId: String,
        partNumber: Int,
        data: ByteArray
    ): Result<String>

    /**
     * 完成分片上传
     */
    fun completeMultipartUpload(
        bucketName: String,
        key: String,
        uploadId: String,
        parts: List<PartInfo>
    ): SimpleResult

    /**
     * 取消分片上传
     */
    fun abortMultipartUpload(
        bucketName: String,
        key: String,
        uploadId: String
    ): SimpleResult

    /**
     * 列出未完成的分片上传
     */
    fun listMultipartUploads(bucketName: String): List<String>

    // ==================== 预签名 URL ====================

    /**
     * 生成预签名下载 URL
     */
    fun generatePresignedUrl(
        bucketName: String,
        key: String,
        expirationSeconds: Long = 3600
    ): PresignedUrl?

    /**
     * 生成预签名上传 URL
     */
    fun generatePresignedUploadUrl(
        bucketName: String,
        key: String,
        contentType: String,
        expirationSeconds: Long = 3600
    ): PresignedUrl?
}

/**
 * S3 客户端工厂接口
 */
interface S3StorageClientFactory {
    /**
     * 创建客户端
     */
    fun createClient(config: S3ClientConfig): S3StorageClient

    /**
     * 创建默认客户端
     */
    fun createDefaultClient(): S3StorageClient
}

/**
 * S3 客户端配置
 */
data class S3ClientConfig(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val region: String = "us-east-1",
    val pathStyleAccess: Boolean = true
)
