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
) {
    /**
     * 从 jdbcUrl 中提取 schema
     */
    val schema: String?
        get() {
            if (jdbcUrl.isNullOrBlank()) return null
            // 通常格式为 jdbc:mysql://host:port/database?params 或 jdbc:mysql://host:port/database
            // 找到最后一个 / 之后且在 ? 之前的部分
            val lastSlashIndex = jdbcUrl.lastIndexOf("/")
            if (lastSlashIndex == -1) return null

            val afterSlash = jdbcUrl.substring(lastSlashIndex + 1)
            val questionMarkIndex = afterSlash.indexOf("?")

            return if (questionMarkIndex != -1) {
                afterSlash.substring(0, questionMarkIndex)
            } else {
                afterSlash
            }.ifBlank { null }
        }
}
