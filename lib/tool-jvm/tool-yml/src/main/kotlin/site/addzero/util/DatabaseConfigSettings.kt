package site.addzero.util

/**
 * 数据库连接配置
 * @param jdbcUrl JDBC URL
 * @param jdbcUsername 用户名
 * @param jdbcPassword 密码
 */
data class DatabaseConfigSettings(
    val jdbcUrl: String?,
    val jdbcUsername: String?,
    val jdbcPassword: String?
)