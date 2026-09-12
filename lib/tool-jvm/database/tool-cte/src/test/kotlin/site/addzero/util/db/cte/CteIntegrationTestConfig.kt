package site.addzero.util.db.cte

internal object CteIntegrationTestConfig {
    val url: String by lazy { requiredEnvironment("ADDZERO_CTE_TEST_JDBC_URL") }
    val username: String by lazy { requiredEnvironment("ADDZERO_CTE_TEST_USERNAME") }
    val password: String by lazy { requiredEnvironment("ADDZERO_CTE_TEST_PASSWORD") }

    private fun requiredEnvironment(name: String): String =
        System.getenv(name)?.takeIf(String::isNotBlank)
            ?: error("Missing environment variable $name")
}
