package site.addzero.rustfs

import org.slf4j.LoggerFactory
import site.addzero.common.result.SimpleResult
import site.addzero.common.result.Result
import site.addzero.rustfs.api.S3ClientConfig
import site.addzero.rustfs.api.S3StorageClient
import site.addzero.rustfs.api.S3StorageClientFactory
import site.addzero.rustfs.impl.AwsS3StorageClient
import site.addzero.rustfs.impl.DefaultS3StorageClientFactory
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.Bucket
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Object
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.File
import java.net.URI
import java.time.Duration

/**
 * RustFS 存储工具类
 *
 * 提供两种使用方式：
 * 1. 使用接口 - 通过 dependency injection 注入 S3StorageClient
 * 2. 使用 AWS SDK 直接调用 - 兼容旧代码
 */
object RustfsUtil {

    private val logger = LoggerFactory.getLogger(RustfsUtil::class.java)

    /**
     * 默认的 S3 客户端工厂
     */
    private val defaultFactory: S3StorageClientFactory by lazy {
        DefaultS3StorageClientFactory {
            S3ClientConfig(
                endpoint = RustfsConfig.default().endpoint,
                accessKey = RustfsConfig.default().accessKey,
                secretKey = RustfsConfig.default().secretKey,
                region = RustfsConfig.default().region
            )
        }
    }

    /**
     * 获取默认的 S3 存储客户端
     */
    fun getDefaultClient(): S3StorageClient {
        return defaultFactory.createDefaultClient()
    }

    /**
     * 创建 AWS SDK S3 客户端（兼容旧代码）
     */
    fun createClient(config: RustfsConfig = RustfsConfig.default()): S3Client {
        val creds = AwsBasicCredentials.create(config.accessKey, config.secretKey)
        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .endpointOverride(URI.create(config.endpoint))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .checksumValidationEnabled(false)
                    .build()
            )
            .region(Region.of(config.region))
            .build()
    }

    /**
     * 创建 S3 存储客户端
     */
    fun createStorageClient(config: RustfsConfig = RustfsConfig.default()): S3StorageClient {
        return defaultFactory.createClient(
            S3ClientConfig(
                endpoint = config.endpoint,
                accessKey = config.accessKey,
                secretKey = config.secretKey,
                region = config.region
            )
        )
    }

    // ==================== 使用 S3StorageClient 的新方法 ====================

    /**
     * 确保存储桶存在（使用 S3StorageClient）
     */
    fun ensureBucket(client: S3StorageClient, bucketName: String): SimpleResult {
        return if (client.bucketExists(bucketName)) {
            SimpleResult.success("Bucket already exists: $bucketName")
        } else {
            client.createBucket(bucketName)
        }
    }

    /**
     * 上传对象（使用 S3StorageClient）
     */
    fun putObject(
        client: S3StorageClient,
        bucketName: String,
        key: String,
        data: ByteArray,
        contentType: String? = null
    ): SimpleResult {
        return client.putObject(bucketName, key, data, contentType)
    }

    /**
     * 上传文件（使用 S3StorageClient）
     */
    fun putObject(
        client: S3StorageClient,
        bucketName: String,
        key: String,
        file: File,
        contentType: String? = null
    ): SimpleResult {
        return client.putObject(bucketName, key, file, contentType)
    }

    /**
     * 获取对象（使用 S3StorageClient）
     */
    fun getObject(client: S3StorageClient, bucketName: String, key: String): ByteArray? {
        return client.getObject(bucketName, key)
    }

    /**
     * 删除对象（使用 S3StorageClient）
     */
    fun deleteObject(client: S3StorageClient, bucketName: String, key: String): SimpleResult {
        return client.deleteObject(bucketName, key)
    }

    /**
     * 批量删除对象（使用 S3StorageClient）
     */
    fun deleteObjects(client: S3StorageClient, bucketName: String, keys: List<String>): SimpleResult {
        return client.deleteObjects(bucketName, keys)
    }

    /**
     * 检查对象是否存在（使用 S3StorageClient）
     */
    fun objectExists(client: S3StorageClient, bucketName: String, key: String): Boolean {
        return client.objectExists(bucketName, key)
    }

    /**
     * 列出对象（使用 S3StorageClient）
     */
    fun listObjects(
        client: S3StorageClient,
        bucketName: String,
        prefix: String? = null,
        recursive: Boolean = true
    ): List<S3StorageClient.ObjectMetadata> {
        return client.listObjects(bucketName, prefix, recursive)
    }

    /**
     * 复制对象（使用 S3StorageClient）
     */
    fun copyObject(
        client: S3StorageClient,
        sourceBucket: String,
        sourceKey: String,
        targetBucket: String,
        targetKey: String
    ): SimpleResult {
        return client.copyObject(sourceBucket, sourceKey, targetBucket, targetKey)
    }

    /**
     * 获取预签名 URL（使用 S3StorageClient）
     */
    fun getPresignedUrl(
        client: S3StorageClient,
        bucketName: String,
        key: String,
        expirationSeconds: Long = 3600
    ): S3StorageClient.PresignedUrl? {
        return client.generatePresignedUrl(bucketName, key, expirationSeconds)
    }

    // ==================== 使用 S3StorageClient 的分片上传方法 ====================

    /**
     * 初始化分片上传
     */
    fun initMultipartUpload(
        client: S3StorageClient,
        bucketName: String,
        key: String,
        contentType: String? = null
    ): Result<String> {
        return client.initMultipartUpload(bucketName, key, contentType)
    }

    /**
     * 上传分片
     */
    fun uploadPart(
        client: S3StorageClient,
        bucketName: String,
        key: String,
        uploadId: String,
        partNumber: Int,
        data: ByteArray
    ): Result<String> {
        return client.uploadPart(bucketName, key, uploadId, partNumber, data)
    }

    /**
     * 完成分片上传
     */
    fun completeMultipartUpload(
        client: S3StorageClient,
        bucketName: String,
        key: String,
        uploadId: String,
        parts: List<S3StorageClient.PartInfo>
    ): SimpleResult {
        return client.completeMultipartUpload(bucketName, key, uploadId, parts)
    }

    /**
     * 取消分片上传
     */
    fun abortMultipartUpload(
        client: S3StorageClient,
        bucketName: String,
        key: String,
        uploadId: String
    ): SimpleResult {
        return client.abortMultipartUpload(bucketName, key, uploadId)
    }

    /**
     * 列出未完成的分片上传
     */
    fun listMultipartUploads(client: S3StorageClient, bucketName: String): List<String> {
        return client.listMultipartUploads(bucketName)
    }

    // ==================== 原有 AWS SDK 方法（保持向后兼容）====================

    /**
     * @deprecated 使用 S3StorageClient.bucketExists 代替
     */
    fun bucketExists(client: S3Client, bucketName: String): Boolean {
        return try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build())
            true
        } catch (_: NoSuchBucketException) {
            false
        } catch (e: Exception) {
            logger.error("Failed to check RustFS bucket {}", bucketName, e)
            false
        }
    }

    /**
     * @deprecated 使用 S3StorageClient 代替
     */
    fun ensureBucket(client: S3Client, bucketName: String): RustfsResult {
        return try {
            if (bucketExists(client, bucketName)) {
                RustfsResult.Success("Bucket already exists: $bucketName")
            } else {
                client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
                RustfsResult.Success("Bucket created: $bucketName")
            }
        } catch (e: Exception) {
            RustfsResult.Error("Failed to ensure bucket: $bucketName", e)
        }
    }

    /**
     * @deprecated 使用 S3StorageClient.listBuckets 代替
     */
    fun listBuckets(client: S3Client): List<Bucket> {
        return try {
            client.listBuckets().buckets()
        } catch (e: Exception) {
            logger.error("Failed to list buckets", e)
            emptyList()
        }
    }

    /**
     * @deprecated 使用 S3StorageClient.putObject 代替
     */
    fun putObject(
        client: S3Client,
        bucketName: String,
        objectName: String,
        data: ByteArray,
        contentType: String = "application/octet-stream"
    ): RustfsResult {
        return try {
            val request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .contentType(contentType)
                .build()
            client.putObject(request, RequestBody.fromBytes(data))
            RustfsResult.Success("Object uploaded: $bucketName/$objectName")
        } catch (e: Exception) {
            RustfsResult.Error("Failed to put object: $bucketName/$objectName", e)
        }
    }

    /**
     * @deprecated 使用 S3StorageClient.putObject 代替
     */
    fun putObject(
        client: S3Client,
        bucketName: String,
        objectName: String,
        file: File,
        contentType: String? = null
    ): RustfsResult {
        return try {
            val builder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
            contentType?.let { builder.contentType(it) }
            client.putObject(builder.build(), RequestBody.fromFile(file))
            RustfsResult.Success("Object uploaded: $bucketName/$objectName")
        } catch (e: Exception) {
            RustfsResult.Error("Failed to put object: $bucketName/$objectName", e)
        }
    }

    /**
     * @deprecated 使用 S3StorageClient.getObject 代替
     */
    fun getObject(client: S3Client, bucketName: String, objectName: String): ByteArray? {
        return try {
            val request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build()
            client.getObjectAsBytes(request).asByteArray()
        } catch (e: NoSuchKeyException) {
            null
        } catch (e: Exception) {
            logger.error("Failed to get object: $bucketName/$objectName", e)
            null
        }
    }

    /**
     * @deprecated 使用 S3StorageClient.deleteObject 代替
     */
    fun deleteObject(client: S3Client, bucketName: String, objectName: String): RustfsResult {
        return try {
            val request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build()
            client.deleteObject(request)
            RustfsResult.Success("Object deleted: $bucketName/$objectName")
        } catch (e: Exception) {
            RustfsResult.Error("Failed to delete object: $bucketName/$objectName", e)
        }
    }

    /**
     * @deprecated 使用 S3StorageClient.deleteObjects 代替
     */
    fun deleteObjects(client: S3Client, bucketName: String, objectNames: Collection<String>): RustfsResult {
        if (objectNames.isEmpty()) {
            return RustfsResult.Success("No objects to delete")
        }
        return try {
            val objects = objectNames.map { key -> ObjectIdentifier.builder().key(key).build() }
            val delete = Delete.builder().objects(objects).build()
            val request = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(delete)
                .build()
            client.deleteObjects(request)
            RustfsResult.Success("Deleted ${objectNames.size} objects from $bucketName")
        } catch (e: Exception) {
            RustfsResult.Error("Failed to delete objects: $bucketName", e)
        }
    }

    /**
     * @deprecated 使用 S3StorageClient.objectExists 代替
     */
    fun objectExists(client: S3Client, bucketName: String, objectName: String): Boolean {
        return try {
            client.headObject(
                HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build()
            )
            true
        } catch (_: NoSuchKeyException) {
            false
        } catch (e: Exception) {
            logger.error("Failed to check object {}/{}", bucketName, objectName, e)
            false
        }
    }

    /**
     * @deprecated 使用 S3StorageClient.listObjects 代替
     */
    fun listObjects(
        client: S3Client,
        bucketName: String,
        prefix: String? = null,
        recursive: Boolean = true
    ): List<S3Object> {
        return try {
            val iterable = client.listObjectsV2Paginator(
                buildListRequest(bucketName, prefix, recursive)
            )
            val objects = mutableListOf<S3Object>()
            for (response in iterable) {
                objects.addAll(response.contents())
            }
            objects
        } catch (e: Exception) {
            logger.error("Failed to list objects: $bucketName", e)
            emptyList()
        }
    }

    /**
     * @deprecated 使用 S3StorageClient.copyObject 代替
     */
    fun copyObject(
        client: S3Client,
        sourceBucket: String,
        sourceKey: String,
        targetBucket: String,
        targetKey: String
    ): RustfsResult {
        return try {
            val request = CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceKey)
                .destinationBucket(targetBucket)
                .destinationKey(targetKey)
                .build()
            client.copyObject(request)
            RustfsResult.Success("Copied $sourceBucket/$sourceKey -> $targetBucket/$targetKey")
        } catch (e: Exception) {
            RustfsResult.Error("Failed to copy object: $sourceBucket/$sourceKey", e)
        }
    }

    /**
     * @deprecated 使用 S3StorageClient.generatePresignedUrl 代替
     */
    fun getPresignedObjectUrl(
        config: RustfsConfig,
        bucketName: String,
        objectName: String,
        expiresInSeconds: Long
    ): String? {
        return try {
            S3Presigner.builder()
                .region(Region.of(config.region))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(config.accessKey, config.secretKey)
                    )
                )
                .endpointOverride(URI.create(config.endpoint))
                .serviceConfiguration(
                    S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build()
                )
                .build()
                .use { presigner ->
                    val request = GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofSeconds(expiresInSeconds))
                        .getObjectRequest(
                            GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(objectName)
                                .build()
                        )
                        .build()
                    presigner.presignGetObject(request).url().toString()
                }
        } catch (e: Exception) {
            logger.error("Failed to presign object: $bucketName/$objectName", e)
            null
        }
    }

    // Visible for testing so we can assert request settings without hitting RustFS.
    internal fun buildListRequest(
        bucketName: String,
        prefix: String?,
        recursive: Boolean
    ): ListObjectsV2Request {
        val builder = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .maxKeys(1000)
        prefix?.takeIf { it.isNotBlank() }?.let { builder.prefix(it) }
        if (!recursive) {
            builder.delimiter("/")
        }
        return builder.build()
    }
}
