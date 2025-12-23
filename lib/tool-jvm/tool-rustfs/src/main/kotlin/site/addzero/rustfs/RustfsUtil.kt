package site.addzero.rustfs

import org.slf4j.LoggerFactory
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

/** Utility helpers that wrap common RustFS (S3-compatible) workflows using the AWS SDK. */
object RustfsUtil {

    private val logger = LoggerFactory.getLogger(RustfsUtil::class.java)

    fun createClient(config: RustfsConfig = RustfsConfig.default()): S3Client {
        val creds = AwsBasicCredentials.create(config.accessKey, config.secretKey)
        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .endpointOverride(URI.create(config.endpoint))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true) // RustFS uses path style like MinIO
                    .checksumValidationEnabled(false)
                    .build()
            )
            .region(Region.of(config.region))
            .build()
    }

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

    fun ensureBucket(client: S3Client, bucketName: String): RustfsResult {
        return try {
            if (bucketExists(client, bucketName)) {
                RustfsResult.Success("Bucket already exists: $bucketName")
            } else {
                client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
                RustfsResult.Success("Bucket created: $bucketName")
            }
        } catch (e: Exception) {
            logError("ensure bucket", bucketName, e)
        }
    }

    fun listBuckets(client: S3Client): List<Bucket> {
        return try {
            client.listBuckets().buckets()
        } catch (e: Exception) {
            logger.error("Failed to list buckets", e)
            emptyList()
        }
    }

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
            logError("put object", "$bucketName/$objectName", e)
        }
    }

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
            logError("put object", "$bucketName/$objectName", e)
        }
    }

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
            logError("get object", "$bucketName/$objectName", e)
            null
        }
    }

    fun deleteObject(client: S3Client, bucketName: String, objectName: String): RustfsResult {
        return try {
            val request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build()
            client.deleteObject(request)
            RustfsResult.Success("Object deleted: $bucketName/$objectName")
        } catch (e: Exception) {
            logError("delete object", "$bucketName/$objectName", e)
        }
    }

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
            logError("delete objects", bucketName, e)
        }
    }

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
            logger.error("Failed to head object {}/{}", bucketName, objectName, e)
            false
        }
    }

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
            collectObjects(iterable)
        } catch (e: Exception) {
            logError("list objects", bucketName, e)
            emptyList()
        }
    }

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
            logError("copy object", "$sourceBucket/$sourceKey", e)
        }
    }

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
            logError("presign object", "$bucketName/$objectName", e)
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

    private fun collectObjects(iterable: ListObjectsV2Iterable): List<S3Object> {
        val objects = mutableListOf<S3Object>()
        for (response in iterable) {
            objects.addAll(response.contents())
        }
        return objects
    }

    private fun logError(action: String, resource: String, e: Exception): RustfsResult.Error {
        val message = "Failed to $action for $resource: ${e.message}"
        logger.error(message, e)
        return RustfsResult.Error(message, e)
    }
}
