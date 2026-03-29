package site.addzero.kcloud.s3

data class S3Config(
    val endpoint: String = "",
    val region: String = "us-east-1",
    val bucket: String = "",
    val accessKey: String = "",
    val secretKey: String = ""
)
