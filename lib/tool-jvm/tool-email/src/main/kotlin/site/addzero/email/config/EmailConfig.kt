package site.addzero.email.config

/**
 * 邮件服务器配置数据类
 *
 * @property host 邮件服务器主机名
 * @property port 邮件服务器端口
 * @property username 用户名
 * @property password 密码或授权码
 * @property protocol 使用的协议 (如: smtp)
 * @property enableSSL 是否启用SSL加密
 * @property enableTLS 是否启用TLS加密
 */
data class EmailConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val protocol: String = "smtp",
    val enableSSL: Boolean = false,
    val enableTLS: Boolean = true
)