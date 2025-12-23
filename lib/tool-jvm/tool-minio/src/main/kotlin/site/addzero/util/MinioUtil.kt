package site.addzero.util

import io.minio.*
import io.minio.messages.Bucket
import io.minio.messages.DeleteError
import io.minio.messages.Item
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

sealed class MinioResult {
    data class Success(val message: String) : MinioResult()
    data class Error(val message: String, val cause: Throwable? = null) : MinioResult()
}

data class MinioConfig(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val region: String? = null
)

data class ObjectInfo(
    val objectName: String,
    val size: Long,
    val etag: String,
    val lastModified: Long?,
    val contentType: String?
)

object MinioUtil {

    private val logger = LoggerFactory.getLogger(MinioUtil::class.java)

    private val clients = mutableMapOf<String, MinioClient>()

    @JvmStatic
    fun createClient(config: MinioConfig): MinioClient {
        val builder = MinioClient.builder()
            .endpoint(config.endpoint)
            .credentials(config.accessKey, config.secretKey)

        config.region?.let { builder.region(it) }

        return builder.build()
    }

    @JvmStatic
    fun createClient(endpoint: String, accessKey: String, secretKey: String): MinioClient {
        return createClient(MinioConfig(endpoint, accessKey, secretKey))
    }

    @JvmStatic
    fun getOrCreateClient(key: String, config: MinioConfig): MinioClient {
        return clients.getOrPut(key) { createClient(config) }
    }

    @JvmStatic
    fun client(config: MinioConfig): MinioClient = createClient(config)

    @JvmStatic
    fun bucketExists(client: MinioClient, bucketName: String): Boolean {
        return try {
            client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())
        } catch (e: Exception) {
            logger.error("Check bucket exists failed: ${e.message}", e)
            false
        }
    }

    @JvmStatic
    fun createBucket(client: MinioClient, bucketName: String): MinioResult {
        return try {
            if (bucketExists(client, bucketName)) {
                MinioResult.Error("Bucket already exists: $bucketName")
            } else {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
                MinioResult.Success("Bucket created: $bucketName")
            }
        } catch (e: Exception) {
            logger.error("Create bucket failed: ${e.message}", e)
            MinioResult.Error("Create bucket failed: ${e.message}", e)
        }
    }

    @JvmStatic
    fun ensureBucket(client: MinioClient, bucketName: String): MinioResult {
        return try {
            if (!bucketExists(client, bucketName)) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
                MinioResult.Success("Bucket created: $bucketName")
            } else {
                MinioResult.Success("Bucket already exists: $bucketName")
            }
        } catch (e: Exception) {
            logger.error("Ensure bucket failed: ${e.message}", e)
            MinioResult.Error("Ensure bucket failed: ${e.message}", e)
        }
    }

    @JvmStatic
    fun listBuckets(client: MinioClient): List<Bucket> {
        return try {
            client.listBuckets()
        } catch (e: Exception) {
            logger.error("List buckets failed: ${e.message}", e)
            emptyList()
        }
    }

    @JvmStatic
    fun deleteBucket(client: MinioClient, bucketName: String, force: Boolean = false): MinioResult {
        return try {
            if (!bucketExists(client, bucketName)) {
                return MinioResult.Error("Bucket not found: $bucketName")
            }

            if (force) {
                val objects = listObjects(client, bucketName)
                if (objects.isNotEmpty()) {
                    val result = deleteObjects(client, bucketName, objects.map { it.objectName })
                    if (result is MinioResult.Error) {
                        return result
                    }
                }
            }

            client.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build())
            MinioResult.Success("Bucket deleted: $bucketName")
        } catch (e: Exception) {
            logger.error("Delete bucket failed: ${e.message}", e)
            MinioResult.Error("Delete bucket failed: ${e.message}", e)
        }
    }

    @JvmStatic
    fun putObject(
        client: MinioClient,
        bucketName: String,
        objectName: String,
        data: ByteArray,
        contentType: String = "application/octet-stream"
    ): MinioResult {
        return try {
            val stream = ByteArrayInputStream(data)
            putObject(client, bucketName, objectName, stream, data.size.toLong(), contentType)
        } catch (e: Exception) {
            logger.error("Put object from bytes failed: ${e.message}", e)
            MinioResult.Error("Put object failed: ${e.message}", e)
        }
    }

    @JvmStatic
    fun putObject(
        client: MinioClient,
        bucketName: String,
        objectName: String,
        file: File,
        contentType: String? = null
    ): MinioResult {
        return try {
            val stream = FileInputStream(file)
            val type = contentType ?: guessContentType(objectName)
            putObject(client, bucketName, objectName, stream, file.length(), type)
        } catch (e: Exception) {
            logger.error("Put object from file failed: ${e.message}", e)
            MinioResult.Error("Put object failed: ${e.message}", e)
        }
    }

    @JvmStatic
    fun putObject(
        client: MinioClient,
        bucketName: String,
        objectName: String,
        stream: InputStream,
        size: Long,
        contentType: String = "application/octet-stream"
    ): MinioResult {
        return try {
            val args = PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .stream(stream, size, -1)
                .contentType(contentType)
                .build()

            client.putObject(args)
            MinioResult.Success("Object uploaded: $bucketName/$objectName")
        } catch (e: Exception) {
            logger.error("Put object failed: ${e.message}", e)
            MinioResult.Error("Put object failed: ${e.message}", e)
        }
    }

    @JvmStatic
    fun getObject(client: MinioClient, bucketName: String, objectName: String): ByteArray? {
        return try {
            client.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build()).use { stream ->
                stream.readBytes()
            }
        } catch (e: Exception) {
            logger.error("Get object failed: ${e.message}", e)
            null
        }
    }

    @JvmStatic
    fun getObjectToFile(client: MinioClient, bucketName: String, objectName: String, file: File): MinioResult {
        return try {
            client.downloadObject(DownloadObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .filename(file.absolutePath)
                .build())
            MinioResult.Success("Object downloaded to: ${file.absolutePath}")
        } catch (e: Exception) {
            logger.error("Get object to file failed: ${e.message}", e)
            MinioResult.Error("Get object to file failed: ${e.message}", e)
        }
    }

    @JvmStatic
    fun statObject(client: MinioClient, bucketName: String, objectName: String): ObjectInfo? {
        return try {
            val stat = client.statObject(StatObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build())

            ObjectInfo(
                objectName = objectName,
                size = stat.size(),
                etag = stat.etag(),
                lastModified = stat.lastModified()?.toInstant()?.toEpochMilli(),
                contentType = stat.contentType()
            )
        } catch (e: Exception) {
            logger.error("Stat object failed: ${e.message}", e)
            null
        }
    }

    @JvmStatic
    fun objectExists(client: MinioClient, bucketName: String, objectName: String): Boolean {
        return statObject(client, bucketName, objectName) != null
    }

    @JvmStatic
    fun listObjects(client: MinioClient, bucketName: String, prefix: String? = null, recursive: Boolean = true): List<ObjectInfo> {
        return try {
            val builder = ListObjectsArgs.builder()
                .bucket(bucketName)
                .recursive(recursive)

            prefix?.let { builder.prefix(it) }

            val results = mutableListOf<ObjectInfo>()
            val iterable = client.listObjects(builder.build())
            for (item in iterable) {
                val result = item.get()
                results.add(
                    ObjectInfo(
                        objectName = result.objectName(),
                        size = result.size(),
                        etag = result.etag(),
                        lastModified = result.lastModified()?.toInstant()?.toEpochMilli(),
                        contentType = null
                    )
                )
            }
            results
        } catch (e: Exception) {
            logger.error("List objects failed: ${e.message}", e)
            emptyList()
        }
    }

    @JvmStatic
    fun deleteObject(client: MinioClient, bucketName: String, objectName: String): MinioResult {
        return try {
            client.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build())
            MinioResult.Success("Object deleted: $bucketName/$objectName")
        } catch (e: Exception) {
            logger.error("Delete object failed: ${e.message}", e)
            MinioResult.Error("Delete object failed: ${e.message}", e)
        }
    }

    @JvmStatic
    fun deleteObjects(client: MinioClient, bucketName: String, objectNames: List<String>): MinioResult {
        return try {
            val objects = objectNames.map { io.minio.messages.DeleteObject(it) }
            val results = client.removeObjects(RemoveObjectsArgs.builder()
                .bucket(bucketName)
                .objects(objects)
                .build())

            val errors = mutableListOf<DeleteError>()
            for (result in results) {
                val error = result.get()
                if (error != null) {
                    errors.add(error)
                }
            }

            if (errors.isNotEmpty()) {
                val errorMsg = errors.joinToString(", ") { "${it.objectName()}: ${it.message()}" }
                MinioResult.Error("Some objects failed to delete: $errorMsg")
            } else {
                MinioResult.Success("Deleted ${objectNames.size} objects")
            }
        } catch (e: Exception) {
            logger.error("Delete objects failed: ${e.message}", e)
            MinioResult.Error("Delete objects failed: ${e.message}", e)
        }
    }

    @JvmStatic
    fun copyObject(
        client: MinioClient,
        sourceBucket: String,
        sourceObject: String,
        targetBucket: String,
        targetObject: String
    ): MinioResult {
        return try {
            client.copyObject(CopyObjectArgs.builder()
                .bucket(targetBucket)
                .`object`(targetObject)
                .source(CopySource.builder()
                    .bucket(sourceBucket)
                    .`object`(sourceObject)
                    .build())
                .build())
            MinioResult.Success("Object copied: $sourceBucket/$sourceObject -> $targetBucket/$targetObject")
        } catch (e: Exception) {
            logger.error("Copy object failed: ${e.message}", e)
            MinioResult.Error("Copy object failed: ${e.message}", e)
        }
    }

    @JvmStatic
    fun getPresignedObjectUrl(client: MinioClient, bucketName: String, objectName: String, expires: Int = 3600): String? {
        return try {
            client.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.GET)
                    .bucket(bucketName)
                    .`object`(objectName)
                    .expiry(expires)
                    .build()
            )
        } catch (e: Exception) {
            logger.error("Get presigned URL failed: ${e.message}", e)
            null
        }
    }

    private fun guessContentType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "pdf" -> "application/pdf"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "txt" -> "text/plain"
            "html" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "zip" -> "application/zip"
            "tar", "gz" -> "application/x-gtar"
            else -> "application/octet-stream"
        }
    }

    private fun InputStream.readBytes(): ByteArray {
        val buffer = java.io.ByteArrayOutputStream()
        val data = ByteArray(4096)
        var nRead: Int
        while (this.read(data).also { nRead = it } != -1) {
            buffer.write(data, 0, nRead)
        }
        return buffer.toByteArray()
    }
}
