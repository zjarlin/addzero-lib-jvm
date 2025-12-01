package site.addzero.util.ssh

data class SshConfig(
    val host: String,
    val port: Int = 22,
    val username: String,
    val password: String? = null,
    val privateKeyPath: String? = null,
    val privateKeyPassphrase: String? = null,
    val connectTimeoutMs: Int = 30000,
    val readTimeoutMs: Int = 60000
) {
    init {
        require(host.isNotBlank()) { "host不能为空" }
        require(username.isNotBlank()) { "username不能为空" }
        require(password != null || privateKeyPath != null) { "password和privateKeyPath至少需要一个" }
    }
}
