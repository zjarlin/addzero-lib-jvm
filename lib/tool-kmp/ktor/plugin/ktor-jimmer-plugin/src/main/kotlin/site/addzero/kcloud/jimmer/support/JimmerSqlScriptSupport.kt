package site.addzero.kcloud.jimmer.support

import javax.sql.DataSource

object JimmerSqlScriptSupport {
    fun loadClasspathSql(
        resourcePath: String,
        classLoader: ClassLoader = defaultClassLoader(),
    ): String {
        val normalizedPath = resourcePath.removePrefix("/")
        return classLoader.getResource(normalizedPath)?.readText().orEmpty()
    }

    fun executeStatements(
        dataSource: DataSource,
        sql: String,
    ) {
        val statements = sql
            .lineSequence()
            .filterNot { line -> line.trimStart().startsWith("--") }
            .joinToString("\n")
            .split(";")
            .map(String::trim)
            .filter(String::isNotBlank)
        if (statements.isEmpty()) {
            return
        }
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statements.forEach(statement::executeUpdate)
            }
        }
    }

    fun executeClasspathSql(
        dataSource: DataSource,
        resourcePath: String,
        classLoader: ClassLoader = defaultClassLoader(),
    ) {
        executeStatements(
            dataSource = dataSource,
            sql = loadClasspathSql(resourcePath = resourcePath, classLoader = classLoader),
        )
    }

    private fun defaultClassLoader(): ClassLoader {
        return Thread.currentThread().contextClassLoader
            ?: JimmerSqlScriptSupport::class.java.classLoader
    }
}
