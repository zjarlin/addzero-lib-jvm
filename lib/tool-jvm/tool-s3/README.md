# tool-s3

> S3 兼容对象存储工具库，统一封装 AWS S3 / MinIO / RustFS / Cloudflare R2 等存储后端的操作，支持分片上传、断点续传和进度追踪。

## 核心能力

### 1. 统一存储客户端接口

[`S3StorageClient`](src/main/kotlin/site/addzero/s3/api/S3StorageClient.kt) 定义了 S3 兼容存储的标准操作：

| 分组 | 操作 |
|---|---|
| **Bucket** | `bucketExists` / `createBucket` / `listBuckets` / `deleteBucket` |
| **对象** | `putObject` / `getObject` / `deleteObject` / `deleteObjects` / `copyObject` / `listObjects` / `objectExists` / `getObjectMetadata` |
| **分片上传** | `initMultipartUpload` / `uploadPart` / `completeMultipartUpload` / `abortMultipartUpload` / `listMultipartUploads` |
| **预签名 URL** | `generatePresignedUrl` / `generatePresignedUploadUrl` |

默认实现 [`AwsS3StorageClient`](src/main/kotlin/site/addzero/s3/AwsS3StorageClient.kt) 基于 AWS SDK v2，可通过 [`DefaultS3StorageClientFactory`](src/main/kotlin/site/addzero/s3/AwsS3StorageClient.kt) 工厂创建。

### 2. RustFS 工具集

| 类 | 职责 |
|---|---|
| [`RustfsConfig`](src/main/kotlin/site/addzero/s3/RustfsConfig.kt) | RustFS 连接配置（endpoint / accessKey / secretKey / region） |
| [`RustfsUtil`](src/main/kotlin/site/addzero/s3/RustfsUtil.kt) | 基础 CRUD 操作工具，同时提供接口方式和 AWS SDK 直连方式 |
| [`RustfsMultipartUtil`](src/main/kotlin/site/addzero/s3/RustfsMultipartUtil.kt) | 分片上传：并发上传、断点续传、进度回调 |
| [`RustfsUploadHelper`](src/main/kotlin/site/addzero/s3/RustfsUploadHelper.kt) | 智能上传：自动根据文件大小选择普通上传或分片上传 |
| [`RustfsResult`](src/main/kotlin/site/addzero/s3/RustfsResult.kt) | 操作结果 sealed class（Success / Error / InProgress） |

### 3. 分片上传与断点续传

- **自动分片**：根据文件大小动态计算分片大小（5MB ~ 100MB）
- **并发上传**：可配置并发数（默认 3）
- **断点续传**：通过 `UploadProgressStorage` 持久化上传状态，中断后可继续
- **重试机制**：可配置重试次数（默认 3 次）
- **智能选择**：`smartUpload` 自动判断文件大小，小于阈值走普通上传，大于阈值走分片上传

```kotlin
val result = RustfsUploadHelper.smartUpload(
    client = s3Client,
    bucketName = "my-bucket",
    objectKey = "large-file.zip",
    file = File("/path/to/large-file.zip"),
    config = MultipartUploadConfig(
        partSize = 10 * 1024 * 1024L,  // 10MB
        concurrency = 5,
        maxRetries = 3
    )
)
```

### 4. 进度追踪

[`UploadProgressStorage`](src/main/kotlin/site/addzero/s3/UploadProgress.kt) 提供三种实现：

| 实现 | 适用场景 |
|---|---|
| `CaffeineUploadProgressStorage` | 高性能本地缓存（默认） |
| `RedisUploadProgressStorage` | 分布式环境，跨实例共享进度 |
| `InMemoryUploadProgressStorage` | 测试 / 无 Caffeine 环境 |

通过 `UploadProgressListener` 接收实时进度回调：

```kotlin
val listener = RustfsUploadHelper.createProgressListener(
    bucketName = "my-bucket",
    objectKey = "video.mp4",
    onUpdate = { progress ->
        println("上传进度: ${progress.formatted}")
    }
)
```

## 快速使用

### 创建客户端

```kotlin
// 方式 1：使用接口
val client = RustfsUtil.createStorageClient(
    RustfsConfig(
        endpoint = "http://localhost:9000",
        accessKey = "minioadmin",
        secretKey = "minioadmin"
    )
)

// 方式 2：使用 AWS SDK 直连（兼容旧代码）
val s3Client = RustfsUtil.createClient(RustfsConfig.default())
```

### 基本操作

```kotlin
// 确保 bucket 存在
RustfsUtil.ensureBucket(client, "my-bucket")

// 上传文件
client.putObject("my-bucket", "docs/readme.txt", File("readme.txt"))

// 下载文件
val data = client.getObject("my-bucket", "docs/readme.txt")

// 生成预签名下载 URL
val url = client.generatePresignedUrl("my-bucket", "docs/readme.txt", expirationSeconds = 3600)

// 列出对象
val objects = client.listObjects("my-bucket", prefix = "docs/")
```

## 依赖

- `software.amazon.awssdk:s3` — AWS SDK v2 S3 客户端
- `org.slf4j:slf4j-api` — 日志门面
- `com.github.ben-manes.caffeine:caffeine` — 本地缓存（进度存储默认实现）
