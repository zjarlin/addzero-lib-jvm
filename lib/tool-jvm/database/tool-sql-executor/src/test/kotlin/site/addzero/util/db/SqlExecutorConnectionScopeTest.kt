package site.addzero.util.db

import org.h2.jdbcx.JdbcDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * 验证连接作用域 SQL 执行能力。
 */
class SqlExecutorConnectionScopeTest {

    @Test
    /**
     * 连接级查询、更新和自增主键应复用同一事务。
     */
    fun shouldSupportConnectionScopedJdbcOperations() {
        createExecutor().use { executor ->
            executor.withTransaction { connection ->
                executor.execute(
                    connection,
                    """
                    CREATE TABLE sample (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL
                    )
                    """.trimIndent(),
                )

                val firstId = executor.insertAndReturnId(connection, "INSERT INTO sample(name) VALUES (?)", "alpha")
                val secondId = executor.insertAndReturnId(connection, "INSERT INTO sample(name) VALUES (?)", "beta")

                assertEquals(listOf(firstId, secondId), executor.queryIds(connection, "SELECT id FROM sample ORDER BY id"))
                assertEquals(2L, executor.queryCount(connection, "SELECT COUNT(1) FROM sample"))
                assertEquals(1, executor.executeUpdate(connection, "UPDATE sample SET name = ? WHERE id = ?", "gamma", firstId))
            }

            executor.withTransaction { connection ->
                val rows = executor.queryForList(connection, "SELECT name FROM sample ORDER BY id")
                assertEquals(listOf("gamma", "beta"), rows.map { row -> row["NAME"] })
            }
        }
    }

    @Test
    /**
     * 事务块抛错时应回滚连接级写入。
     */
    fun shouldRollbackConnectionScopedChangesWhenTransactionFails() {
        createExecutor().use { executor ->
            executor.execute(
                """
                CREATE TABLE sample (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL
                )
                """.trimIndent(),
            )

            assertFailsWith<IllegalStateException> {
                executor.withTransaction { connection ->
                    executor.executeUpdate(connection, "INSERT INTO sample(id, name) VALUES (?, ?)", 1, "rollback")
                    error("rollback")
                }
            }

            assertEquals(0L, executor.queryCount("SELECT COUNT(1) FROM sample"))
        }
    }

    private fun createExecutor(): SqlExecutor {
        val dataSource = JdbcDataSource()
        dataSource.setURL("jdbc:h2:mem:sql-executor-connection-scope-${System.nanoTime()};DB_CLOSE_DELAY=-1")
        dataSource.user = "sa"
        dataSource.password = ""
        return SqlExecutor(dataSource)
    }
}
