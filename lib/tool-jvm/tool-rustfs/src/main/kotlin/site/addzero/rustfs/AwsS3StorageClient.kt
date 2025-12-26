package site.addzero.rustfs.impl

import site.addzero.common.result.SimpleResult
import site.addzero.common.result.Result
import site.addzero.rustfs.api.S3ClientConfig
import site.addzero.rustfs.api.S3StorageClient
import site.addzero.rustfs.api.S3StorageClientFactory
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.File
import java.net.URI
import java.time.Instant
import java.time.Duration

/**
 * 基于 AWS SDK 的 S3 存储客户端默认实现
 *
 * 支持 S3、MinIO、RustFS 等兼容 S3 协议的存储服务。
 */
class AwsS3StorageClient(
    private val client: S3Client,
    private val config: S3ClientConfig
) : S3StorageClient {

    private val presigner: S3Presigner by lazy {
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
                    .pathStyleAccessEnabled(config.pathStyleAccess)
                    .build()
            )
            .build()
    }

    // ==================== Bucket 操作 ====================

    override fun bucketExists(bucketName: String): Boolean {
        return try {
            client.headBucket(
                HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build()
            )
            true
        } catch (e: NoSuchBucketException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    override fun createBucket(bucketName: String): SimpleResult {
        return SimpleResult.catch("Failed to create bucket: $bucketName") {
            client.createBucket(
                CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build()
            )
            "Bucket created: $bucketName"
        }
    }

    override fun listBuckets(): List<String> {
        return try {
            client.listBuckets().buckets().map { it.name() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun deleteBucket(bucketName: String): SimpleResult {
        return SimpleResult.catch("Failed to delete bucket: $bucketName") {
            client.deleteBucket(
                DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build()
            )
            "Bucket deleted: $bucketName"
        }
    }

    // ==================== 对象操作 ====================

    override fun objectExists(bucketName: String, key: String): Boolean {
        return try {
            client.headObject(
                HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build()
            )
            true
        } catch (e: NoSuchKeyException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    override fun getObjectMetadata(bucketName: String, key: String): S3StorageClient.ObjectMetadata? {
        return try {
            val response = client.headObject(
                HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build()
            )
            S3StorageClient.ObjectMetadata(
                key = key,
                size = response.contentLength(),
                etag = response.eTag(),
                lastModified = response.lastModified(),
                contentType = response.contentType(),
                metadata = response.metadata()?.toMap() ?: emptyMap()
            )
        } catch (e: NoSuchKeyException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    override fun putObject(
        bucketName: String,
        key: String,
        data: ByteArray,
        contentType: String?,
        metadata: Map<String, String>
    ): SimpleResult {
        return SimpleResult.catch("Failed to upload object: $bucketName/$key") {
            val builder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)

            contentType?.let { builder.contentType(it) }
            if (metadata.isNotEmpty()) {
                builder.metadata(metadata)
            }

            client.putObject(builder.build(), RequestBody.fromBytes(data))
            "Object uploaded: $bucketName/$key"
        }
    }

    override fun putObject(
        bucketName: String,
        key: String,
        file: File,
        contentType: String?,
        metadata: Map<String, String>
    ): SimpleResult {
        return SimpleResult.catch("Failed to upload file: $bucketName/$key") {
            val builder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)

            contentType?.let { builder.contentType(it) }
            if (metadata.isNotEmpty()) {
                builder.metadata(metadata)
            }

            client.putObject(builder.build(), RequestBody.fromFile(file))
            "File uploaded: $bucketName/$key"
        }
    }

    override fun getObject(bucketName: String, key: String): ByteArray? {
        return try {
            client.getObjectAsBytes(
                GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build()
            ).asByteArray()
        } catch (e: NoSuchKeyException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    override fun deleteObject(bucketName: String, key: String): SimpleResult {
        return SimpleResult.catch("Failed to delete object: $bucketName/$key") {
            client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build()
            )
            "Object deleted: $bucketName/$key"
        }
    }

    override fun deleteObjects(bucketName: String, keys: List<String>): SimpleResult {
        if (keys.isEmpty()) {
            return SimpleResult.success("No objects to delete")
        }
        return SimpleResult.catch("Failed to delete objects from $bucketName") {
            val objects = keys.map { k ->
                ObjectIdentifier.builder().key(k).build()
            }
            val delete = Delete.builder().objects(objects).build()
            client.deleteObjects(
                DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(delete)
                    .build()
            )
            "Deleted ${keys.size} objects from $bucketName"
        }
    }

    override fun copyObject(
        sourceBucket: String,
        sourceKey: String,
        targetBucket: String,
        targetKey: String
    ): SimpleResult {
        return SimpleResult.catch("Failed to copy $sourceBucket/$sourceKey to $targetBucket/$targetKey") {
            client.copyObject(
                CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(targetBucket)
                    .destinationKey(targetKey)
                    .build()
            )
            "Copied $sourceBucket/$sourceKey -> $targetBucket/$targetKey"
        }
    }

    override fun listObjects(
        bucketName: String,
        prefix: String?,
        recursive: Boolean,
        maxKeys: Int
    ): List<S3StorageClient.ObjectMetadata> {
        return try {
            val builder = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(maxKeys)

            prefix?.takeIf { it.isNotBlank() }?.let { builder.prefix(it) }
            if (!recursive) {
                builder.delimiter("/")
            }

            val iterable = client.listObjectsV2Paginator(builder.build())
            val objects = mutableListOf<S3StorageClient.ObjectMetadata>()

            for (response in iterable) {
                for (obj in response.contents()) {
                    objects.add(
                        S3StorageClient.ObjectMetadata(
                            key = obj.key(),
                            size = obj.size(),
                            etag = obj.eTag(),
                            lastModified = obj.lastModified(),
                            contentType = null,
                            metadata = emptyMap()
                        )
                    )
                }
            }
            objects
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ==================== 分片上传操作 ====================

    override fun initMultipartUpload(
        bucketName: String,
        key: String,
        contentType: String?,
        metadata: Map<String, String>
    ): Result<String> {
        return Result.catch("Failed to initialize multipart upload: $bucketName/$key") {
            val builder = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)

            contentType?.let { builder.contentType(it) }
            if (metadata.isNotEmpty()) {
                builder.metadata(metadata)
            }

            val response = client.createMultipartUpload(builder.build())
            response.uploadId()
        }
    }

    override fun uploadPart(
        bucketName: String,
        key: String,
        uploadId: String,
        partNumber: Int,
        data: ByteArray
    ): Result<String> {
        return Result.catch("Failed to upload part $partNumber for $bucketName/$key") {
            val request = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .contentLength(data.size.toLong())
                .build()

            val response = client.uploadPart(request, RequestBody.fromBytes(data))
            response.eTag() ?: ""
        }
    }

    override fun completeMultipartUpload(
        bucketName: String,
        key: String,
        uploadId: String,
        parts: List<S3StorageClient.PartInfo>
    ): SimpleResult {
        return SimpleResult.catch("Failed to complete multipart upload: $bucketName/$key") {
            val completedParts = parts.map { part ->
                CompletedPart.builder()
                    .partNumber(part.partNumber)
                    .eTag(part.etag ?: "")
                    .build()
            }

            val request = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload { m -> m.parts(completedParts) }
                .build()

            client.completeMultipartUpload(request)
            "Multipart upload completed: $bucketName/$key"
        }
    }

    override fun abortMultipartUpload(
        bucketName: String,
        key: String,
        uploadId: String
    ): SimpleResult {
        return SimpleResult.catch("Failed to abort multipart upload: $bucketName/$key") {
            client.abortMultipartUpload(
                AbortMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .uploadId(uploadId)
                    .build()
            )
            "Multipart upload aborted: $bucketName/$key"
        }
    }

    override fun listMultipartUploads(bucketName: String): List<String> {
        return try {
            client.listMultipartUploads(
                ListMultipartUploadsRequest.builder()
                    .bucket(bucketName)
                    .build()
            ).uploads().map { it.key() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ==================== 预签名 URL ====================

    override fun generatePresignedUrl(
        bucketName: String,
        key: String,
        expirationSeconds: Long
    ): S3StorageClient.PresignedUrl? {
        return try {
            val request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expirationSeconds))
                .getObjectRequest(
                    GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
                )
                .build()

            val presignedRequest = presigner.presignGetObject(request)
            S3StorageClient.PresignedUrl(
                url = presignedRequest.url().toString(),
                expiration = presignedRequest.expiration()
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun generatePresignedUploadUrl(
        bucketName: String,
        key: String,
        contentType: String,
        expirationSeconds: Long
    ): S3StorageClient.PresignedUrl? {
        // PresignedPutObjectRequest API varies between AWS SDK versions
        // Returning null for now as this is a less common feature
        return null
    }
}

/**
 * 默认的 S3 客户端工厂
 */
class DefaultS3StorageClientFactory(
    private val defaultConfig: () -> S3ClientConfig
) : S3StorageClientFactory {

    override fun createClient(config: S3ClientConfig): S3StorageClient {
        val awsConfig = AwsBasicCredentials.create(config.accessKey, config.secretKey)
        val s3Client = S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsConfig))
            .endpointOverride(URI.create(config.endpoint))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(config.pathStyleAccess)
                    .checksumValidationEnabled(false)
                    .build()
            )
            .region(Region.of(config.region))
            .build()

        return AwsS3StorageClient(s3Client, config)
    }

    override fun createDefaultClient(): S3StorageClient {
        return createClient(defaultConfig())
    }
}
