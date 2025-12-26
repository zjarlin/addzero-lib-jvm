package site.addzero.rustfs

/**
 * Basic connection information for a RustFS cluster.
 *
 * The default values follow the public RustFS quick-start docs:
 * - endpoint: http://127.0.0.1:9000
 * - credentials: rustfsadmin / rustfsadmin
 * - region: us-east-1 (RustFS uses a single-region setup but the AWS SDK requires one)
 */
data class RustfsConfig(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val region: String = "us-east-1"
) {
    companion object {
        fun default(): RustfsConfig = RustfsConfig(
            endpoint = "http://localhost:9000",
            accessKey = "rustfsadmin",
            secretKey = "rustfsadmin",
            region = "us-east-1"
        )
    }
}
