package site.addzero.util

/**
 * 数据库配置读取器
 * 从 Spring Boot YAML 配置文件读取数据库连接配置
 */
object DatabaseConfigReader {

    /**
     * 尝试的所有单数据源配置路径
     * 按优先级排序，常见的配置路径在前
     */
    private val SINGLE_DATASOURCE_PATHS = listOf(
        // Spring Boot 标准 JDBC 配置
        "spring.datasource.url",
        "spring.datasource.jdbc-url",
        // Spring R2DBC 配置
        "spring.r2dbc.url",
        // 旧版 Spring Boot 配置
        "spring.datasource.primary.url",
        "spring.datasource.master.url",
        // 可能的变体
        "spring.datasource.default.url",
        "spring.data.jdbc.url"
    )

    /**
     * 可能的多数据源配置路径（用于注释说明）
     *
     * ### 动态数据源配置
     * ```yaml
     * spring:
     *   datasource:
     *     dynamic:
     *       enabled: true
     *       default-target: master
     *       datasource:
     *         master:
     *           url: jdbc:mysql://localhost:3306/master
     *           username: root
     *           password: password
     *         slave:
     *           url: jdbc:mysql://localhost:3306/slave
     *           username: root
     *           password: password
     * ```
     *
     * ### 标准 Spring 多数据源配置
     * ```yaml
     * spring:
     *   datasource:
     *     master:
     *       url: jdbc:mysql://localhost:3306/master
     *       username: root
     *       password: password
     *     slave:
     *       url: jdbc:mysql://localhost:3306/slave
     *       username: root
     *       password: password
     * ```
     *
     * ### 自定义命名多数据源
     * ```yaml
     * spring:
     *   datasource:
     *     ds1:
     *       url: jdbc:mysql://localhost:3306/db1
     *       username: root
     *       password: password
     *     ds2:
     *       url: jdbc:mysql://localhost:3306/db2
     *       username: root
     *       password: password
     * ```
     *
     * ### MyBatis-Plus 多数据源配置
     * ```yaml
     * spring:
     *   datasource:
     *     mp:
     *       enabled: true
     *       datasource:
     *         master:
     *           url: jdbc:mysql://localhost:3306/master
     *           username: root
     *           password: password
     * ```
     */
    private val MULTI_DATASOURCE_PATH_PATTERNS = listOf(
        "spring.datasource.dynamic.datasource.{name}.url",
        "spring.datasource.mp.datasource.{name}.url",
        "spring.datasource.{name}.url",
        "spring.datasource.{name}.jdbc-url"
    )

    /**
     * 从 Spring Boot 配置文件读取数据库配置
     * 自动尝试多种配置路径，支持常见的数据源配置格式
     *
     * @param customYmlPath 自定义的配置文件路径，为 null 时使用默认路径
     * @param preferDataSourceName 指定优先使用的数据源名称（如 "master", "slave"）
     *                             用于多数据源环境中选择特定数据源
     * @return 数据库连接配置，如果未找到则返回 null
     */
    fun fromSpringYml(
        customYmlPath: String? = null,
        preferDataSourceName: String? = null
    ): DatabaseConfigSettings? {
        return try {
            val ymlUtil = SpringYmlUtil(customYmlPath)

            // 如果指定了数据源名称，优先尝试读取该数据源
            if (preferDataSourceName != null) {
                val namedConfig = readNamedDataSource(ymlUtil, preferDataSourceName)
                if (namedConfig != null) {
                    return namedConfig
                }
            }

            // 尝试所有单数据源配置路径
            for (urlPath in SINGLE_DATASOURCE_PATHS) {
                val url = ymlUtil.getActivateYmlPropertiesString(urlPath)
                if (url != null) {
                    // 根据找到的 URL 路径，推断对应的 username 和 password 路径
                    val basePath = extractBasePath(urlPath)
                    val usernamePath = "$basePath.username"
                    val passwordPath = "$basePath.password"

                    val username = ymlUtil.getActivateYmlPropertiesString(usernamePath)
                        ?: ymlUtil.getActivateYmlPropertiesString("spring.datasource.username")
                        ?: ymlUtil.getActivateYmlPropertiesString("spring.r2dbc.username")

                    val password = ymlUtil.getActivateYmlPropertiesString(passwordPath)
                        ?: ymlUtil.getActivateYmlPropertiesString("spring.datasource.password")
                        ?: ymlUtil.getActivateYmlPropertiesString("spring.r2dbc.password")

                    return DatabaseConfigSettings(
                        jdbcUrl = url,
                        jdbcUsername = username,
                        jdbcPassword = password
                    )
                }
            }

            // 尝试从常见的命名数据源中读取（按优先级）
            val commonDataSourceNames = listOf("master", "primary", "default", "main", "slave")
            for (dsName in commonDataSourceNames) {
                val config = readNamedDataSource(ymlUtil, dsName)
                if (config != null) {
                    return config
                }
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从指定名称的数据源读取配置
     * 支持多种多数据源配置格式
     */
    private fun readNamedDataSource(
        ymlUtil: SpringYmlUtil,
        dataSourceName: String
    ): DatabaseConfigSettings? {
        // 尝试多种命名数据源的配置路径
        val urlPaths = listOf(
            "spring.datasource.$dataSourceName.url",
            "spring.datasource.$dataSourceName.jdbc-url",
            "spring.datasource.dynamic.datasource.$dataSourceName.url",
            "spring.datasource.mp.datasource.$dataSourceName.url"
        )

        for (urlPath in urlPaths) {
            val url = ymlUtil.getActivateYmlPropertiesString(urlPath)
            if (url != null) {
                val basePath = extractBasePath(urlPath)
                val username = ymlUtil.getActivateYmlPropertiesString("$basePath.username") ?: ""
                val password = ymlUtil.getActivateYmlPropertiesString("$basePath.password") ?: ""

                return DatabaseConfigSettings(
                    jdbcUrl = url,
                    jdbcUsername = username,
                    jdbcPassword = password
                )
            }
        }

        return null
    }

    /**
     * 从 URL 路径中提取基础路径
     * 例如: "spring.datasource.url" -> "spring.datasource"
     *       "spring.datasource.jdbc-url" -> "spring.datasource"
     */
    private fun extractBasePath(urlPath: String): String {
        return urlPath.substringBeforeLast(".")
            .removeSuffix(".jdbc")
            .removeSuffix(".r2dbc")
    }
}
