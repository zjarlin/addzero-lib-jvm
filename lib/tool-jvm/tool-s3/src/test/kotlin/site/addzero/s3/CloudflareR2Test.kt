package site.addzero.s3

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import site.addzero.s3.api.S3ClientConfig
import site.addzero.s3.api.S3StorageClient
import java.util.*

/**
 * Cloudflare R2 连通性测试
 *
 * R2 使用方式：
 * 1. 在 R2 控制台创建 API Token，拿到 Access Key ID 和 Secret Access Key
 * 2. Account ID 在 R2 控制台右上角显示
 * 3. Endpoint: https://<ACCOUNT_ID>.r2.cloudflarestorage.com
 *
 * 运行测试：
 * ./gradlew :lib:tool-jvm:tool-rustfs:test --tests "site.addzero.rustfs.CloudflareR2Test"
 */
@TestInstance(Lifecycle.PER_CLASS)
class CloudflareR2Test {

    /**
     * 方式1: 直接配置（把这里换成你的 R2 信息）
     */
    private val config = S3ClientConfig(
        endpoint = "https://2e8161bcd7228307ce4cf2eea44b30f9.r2.cloudflarestorage.com",
        accessKey = "c9ae612973402d470afc193486ea1cf2",
        secretKey = "086cf3212cae336beab202d3e44a069fd4888da286f4c1d7484e7924e59a7518",
        region = "auto",  // R2 用 "auto"
        pathStyleAccess = true  // R2 需要 path style
    )


    // 从环境变量读取 Bucket 名称，默认 test-bucket
    private val bucketName = System.getenv("R2_BUCKET") ?: "cloud-s3"

    private lateinit var factory: DefaultS3StorageClientFactory
    private lateinit var client: S3StorageClient

    @BeforeAll
    fun setup() {
        factory = DefaultS3StorageClientFactory { config }
        client = factory.createClient(config)

        println("=" * 60)
        println("Cloudflare R2 连通性测试")
        println("=" * 60)
        println("Bucket: $bucketName")
        println()
    }

    @Test
    @Order(1)
    fun `测试 - 连接并列出所有 Buckets`() {
        println("[1/5] 测试连接...")

        val buckets = client.listBuckets()

        println("✓ 连接成功!")
        println("  Buckets 数量: ${buckets.size}")
        buckets.forEach { println("    - $it") }

        // R2 要求 bucket 必须存在才能操作
        if (!buckets.contains(bucketName)) {
            println("⚠ 警告: Bucket '$bucketName' 不存在，请先创建")
        }
    }

    @Test
    @Order(2)
    fun `测试 - Bucket 存在检查`() {
        println("[2/5] 检查 Bucket 存在...")

        val exists = client.bucketExists(bucketName)
        println("✓ Bucket '$bucketName' 存在: $exists")

        // R2 需要预先创建 bucket，不会自动创建
        assumeTrue(exists, "R2 bucket 不存在，跳过后续测试。请先在控制台创建 bucket: $bucketName")
    }

    @Test
    @Order(3)
    fun `测试 - 上传对象`() {
        println("[3/5] 测试上传...")
        val testKey = "r2-test-${UUID.randomUUID()}.txt"
        val content = "Hello Cloudflare R2! 测试内容 - ${System.currentTimeMillis()}".toByteArray()

        val result = client.putObject(
            bucketName = bucketName,
            key = testKey,
            data = content,
            contentType = "text/plain"
        )

        assertTrue(result.success, "上传应该成功: ${result.message}")
        println("✓ 上传成功: $testKey")

        // 验证
        assertTrue(client.objectExists(bucketName, testKey), "对象应该存在")
        println("✓ 对象存在验证通过")

        // 清理
        client.deleteObject(bucketName, testKey)
        println("✓ 清理完成")
    }

    @Test
    @Order(4)
    fun `测试 - 下载对象`() {
        println("[4/5] 测试下载...")
        val testKey = "r2-download-${UUID.randomUUID()}.txt"
        val content = "R2 download test - ${System.currentTimeMillis()}".toByteArray()

        // 上传
        client.putObject(bucketName, testKey, content, "text/plain")

        // 下载
        val downloaded = client.getObject(bucketName, testKey)
        assertNotNull(downloaded, "应该能下载到对象")
        assertArrayEquals(content, downloaded, "内容应该一致")
        println("✓ 下载成功，内容一致")

        // 清理
        client.deleteObject(bucketName, testKey)
        println("✓ 清理完成")
    }

    @Test
    @Order(5)
    fun `测试 - 生成预签名 URL`() {
        println("[5/5] 测试预签名 URL...")
        val testKey = "r2-presign-${UUID.randomUUID()}.txt"
        val content = "Presign test".toByteArray()

        // 上传
        val result = client.putObject(bucketName, testKey, content, "text/plain")
        assertTrue(result.success, "上传应该成功: ${result.message}")
        println("✓ 上传成功: $testKey")

        // 生成 URL (1小时有效)
        val presignedUrl = client.generatePresignedUrl(bucketName, testKey, 3600)

        if (presignedUrl != null) {
            println("✓ 预签名 URL 生成成功")
            println("  URL: ${presignedUrl.url}")
            println("  过期时间: ${presignedUrl.expiration}")

            // R2 预签名 URL 可以直接用 curl 测试
            println("  测试命令: curl -I \"${presignedUrl.url}\"")
        } else {
            println("⚠ 预签名 URL 生成失败（可能不支持）")
        }

        // 清理（延迟 5 秒，方便复制 URL 测试）
        println("等待 5 秒后清理，可在此期间复制 URL 测试...")
        Thread.sleep(5000)
        client.deleteObject(bucketName, testKey)
        println("✓ 清理完成")
    }

    @Test
    @Disabled("完整流程测试 - 手动启用")
    fun `R2 完整流程测试`() {
        println("[R2 完整流程测试]")
        val testKey = "r2-full-${UUID.randomUUID()}.txt"
        val content = "R2 full flow test".toByteArray()

        try {
            // 1. 上传
            println("1. 上传...")
            val upload = client.putObject(bucketName, testKey, content, "text/plain")
            assertTrue(upload.success)
            println("   ✓ 上传成功")

            // 2. 元数据
            println("2. 获取元数据...")
            val meta = client.getObjectMetadata(bucketName, testKey)
            println("   ✓ size=${meta?.size}, etag=${meta?.etag}")

            // 3. 下载
            println("3. 下载...")
            val data = client.getObject(bucketName, testKey)
            assertArrayEquals(content, data)
            println("   ✓ 内容一致")

            // 4. 复制
            println("4. 复制...")
            val copyKey = "r2-copy-${UUID.randomUUID()}.txt"
            val copy = client.copyObject(bucketName, testKey, bucketName, copyKey)
            assertTrue(copy.success)
            println("   ✓ 复制成功: $copyKey")

            // 5. 删除复制
            println("5. 删除复制对象...")
            client.deleteObject(bucketName, copyKey)
            println("   ✓ 已删除")

        } finally {
            // 清理
            if (client.objectExists(bucketName, testKey)) {
                client.deleteObject(bucketName, testKey)
                println("   ✓ 清理完成")
            }
        }
    }

    /**
     * 生成浏览器可访问的链接并打开
     * 运行: ./gradlew :lib:tool-jvm:tool-rustfs:test --tests "site.addzero.rustfs.CloudflareR2Test.生成浏览器链接并打开"
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "R2_ACCESS_KEY", matches = ".+")
    fun `生成浏览器链接并打开`() {
        println("[生成浏览器可访问链接]")

        // 创建一个简单的 HTML 文件
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>R2 Test</title>
                <style>
                    body { font-family: Arial, sans-serif; padding: 40px; background: #1a1a2e; color: #eee; }
                    h1 { color: #f39c12; }
                    .info { background: #16213e; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .url { background: #0f3460; padding: 10px; border-radius: 4px; word-break: break-all; }
                </style>
            </head>
            <body>
                <h1>Cloudflare R2 测试页面</h1>
                <div class="info">
                    <p><strong>生成时间:</strong> ${Date()}</p>
                    <p><strong>Bucket:</strong> $bucketName</p>
                    <p><strong>Account:</strong> 2e8161bcd7228307ce4cf2eea44b30f9</p>
                </div>
                <p>如果看到这个页面，说明 R2 预签名 URL 工作正常！</p>
            </body>
            </html>
        """.trimIndent()

        val testKey = "r2-browser-test-${System.currentTimeMillis()}.html"

        try {
            // 1. 上传 HTML 文件
            println("1. 上传 HTML 文件...")
            val upload = client.putObject(
                bucketName = bucketName,
                key = testKey,
                data = htmlContent.toByteArray(),
                contentType = "text/html"
            )
            assertTrue(upload.success, "上传失败: ${upload.message}")
            println("   ✓ 上传成功: $testKey")

            // 2. 生成预签名 URL（7天有效）
            println("2. 生成预签名 URL...")
            val presignedUrl = client.generatePresignedUrl(bucketName, testKey, 7 * 24 * 3600)
            assertNotNull(presignedUrl, "生成 URL 失败")

            val url = presignedUrl!!.url
            println("   ✓ URL 生成成功")
            println()
            println("=" * 60)
            println("浏览器访问链接:")
            println(url)
            println("=" * 60)
            println()

            // 3. 输出 curl 命令供测试
            println("curl 测试命令:")
            println("curl -I \"$url\"")
            println()

            // 4. 尝试用 Chrome MCP 打开（如果可用）
            tryOpenInBrowser(url)

        } finally {
            // 清理（注释掉这行可以保留文件供手动测试）
            // client.deleteObject(bucketName, testKey)
            // println("✓ 文件已删除")
            println("✓ 文件保留在 bucket 中: $testKey")
        }
    }

    /**
     * 尝试在浏览器中打开链接
     */
    private fun tryOpenInBrowser(url: String) {
        println("尝试在浏览器中打开...")
        try {
            // 使用 Mac open 命令
            val process = ProcessBuilder("open", url)
                .inheritIO()
                .start()
            process.waitFor()
            println("✓ 已在浏览器中打开")
        } catch (e: Exception) {
            println("⚠ 无法自动打开浏览器: ${e.message}")
            println("  请手动复制上面的 URL 到浏览器中访问")
        }
    }

    private operator fun String.times(n: Int): String = repeat(n)
}
