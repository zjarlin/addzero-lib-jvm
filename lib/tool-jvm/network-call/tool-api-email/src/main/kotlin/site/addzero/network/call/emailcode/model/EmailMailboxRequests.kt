package site.addzero.network.call.emailcode.model

enum class EmailMailboxSecretType {
    PASSWORD,
    OAUTH2_ACCESS_TOKEN,
}

data class EmailMailboxCreateRequest(
    val prefix: String = "az",
    val passwordLength: Int = 12,
    val preferredDomain: String? = null,
)

data class EmailMailboxLoginRequest(
    val address: String,
    val credential: String,
    val secretType: EmailMailboxSecretType = EmailMailboxSecretType.PASSWORD,
    val folderName: String? = null,
    val connectTimeoutMs: Int = 10_000,
    val readTimeoutMs: Int = 20_000,
    val writeTimeoutMs: Int = 20_000,
)
